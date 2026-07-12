package org.koppe.cuf.mail.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.koppe.cuf.mail.server.common.Authenticator;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.HttpCode;
import org.koppe.cuf.mail.server.http.entities.JwtAuthenticator;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.RequestBody;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;

@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class HttpSession<I, O> implements Session {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(HttpSession.class);
    /**
     * Signalises system activity
     */
    @Getter
    @Include
    private volatile boolean active = true;
    /**
     * Session socket
     */
    @Include
    private final Socket socket;
    /**
     * First line the server needs to determine method and endpoint
     */
    private final FirstLine firstLine;
    /**
     * Endpoint called by the client
     */
    @Include
    private final Endpoint<I, O> endpoint;
    /**
     * Reader for the input stream
     */
    private final BufferedReader reader;
    /**
     * Writer for the output stream
     */
    private final PrintWriter writer;
    /**
     * List of server headers
     */
    private Map<String, String> serverHeaders = Map.of("Server", "Host", "Cache-Control", "no-store", "Connection",
            "close");
    /**
     * List of all subscribed servers
     */
    private List<Server> subscribers = new ArrayList<>();
    /**
     * Authenticator
     */
    @Setter
    private Authenticator auth = new JwtAuthenticator();

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        subscribers.forEach(s -> s.notify(new StatusChangeEvent(this, StatusChange.START)));
        Request<I> request = Request.empty(endpoint.getInputType());

        subscribers.forEach(s -> s.notify(new StatusChangeEvent(this, StatusChange.WORKING)));
        try {
            buildRequest(request);
        } catch (IOException e) {
            logger.info("Exception occurred while building the request", e);
            close();
            subscribers.forEach(s -> s.notify(new StatusChangeEvent(this, StatusChange.DONE)));
            return;
        }

        @SuppressWarnings("unused")
        User user = null;
        Response<O> response = null;

        if (endpoint.isAuthenticated() && (user = auth.authenticate(request)) == null) {
            logger.debug("Session is not authorized, closing ");
            response = Response.unauthorized();
        } else {
            logger.debug("Givign built request to endpoint to handle");
            response = endpoint.handle(request);
            logger.debug("Endpoint handled request, initializing response handling");
        }

        try {
            handleResponse(response);
        } catch (IOException e) {
            logger.info("Could not respond to client due to exception", e);
            try {
                errorHandling();
            } catch (IOException e1) {
                logger.error("Exception occurred while handling the previous exception", e1);
            }

        }
        close();
        subscribers.forEach(s -> s.notify(new StatusChangeEvent(this, StatusChange.DONE)));
    }

    // #region build request
    /**
     * Builds the request by reading the sockets input stream
     * 
     * @param request Request to populate
     * @throws IOException
     */
    private void buildRequest(Request<I> request) throws IOException {
        logger.debug("Building request");
        Map<String, String> headers = new HashMap<>();

        String line;
        logger.debug("Starting to read client input");
        while ((line = reader.readLine()) != null) {
            logger.trace("Read new line");
            if (line.isBlank()) {
                logger.debug("End of headers");
                break;
            }

            String[] lineParts = line.split(": ", 2);
            if (lineParts.length == 2) {
                headers.put(lineParts[0], lineParts[1]);
                logger.trace("Header appended");
            } else {
                logger.warn("Invalid header line: {}", line);
            }
        }

        if (line == null) {
            throw new IOException("Client closed connection before end of headers");
        }

        request.setHeaders(headers);
        addQuery(request);
        logger.debug("Added query to request");

        int contentLength = 0;
        if (headers.containsKey("Content-Length")) {
            try {
                contentLength = Integer.parseInt(headers.get("Content-Length").trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid Content-Length: {}", headers.get("Content-Length"));
                request.setBody(RequestBody.empty(endpoint.getInputType()));
                return;
            }
        }

        String body = "";
        if (contentLength > 0) {
            body = readBody(contentLength);
        }

        if (endpoint.getInputType().equals(new TypeReference<Void>() {
        })) {
            logger.debug("Expected type is Void, ignoring body");
            request.setBody(RequestBody.empty(endpoint.getInputType()));
        } else if (body.isBlank()) {
            logger.debug("No body sent, using empty body");
            request.setBody(RequestBody.empty(endpoint.getInputType()));
        } else {
            I parsed = parseBody(body);
            if (parsed == null) {
                logger.warn("Could not parse body to object");
                request.setBody(RequestBody.empty(endpoint.getInputType()));
                return;
            }
            request.setBody(RequestBody.of(parsed, body, endpoint.getInputType()));
        }

        logger.info("Built request");
    }

    /**
     * Reads the number of bytes given in length, to extract body from the input
     * stream.
     * 
     * @param length Length of the body
     * @return Read body
     * @throws IOException If the stream could not be read
     */
    private String readBody(int length) throws IOException {
        char[] buffer = new char[length];
        int read = 0;
        while (read < length) {
            int n = reader.read(buffer, read, length - read);
            if (n < 0) {
                break;
            }
            read += n;
        }
        return new String(buffer, 0, read);
    }

    // #region parse body
    /**
     * Parse request body
     * 
     * @param body Body to parse
     * @return Parsed body
     */
    private I parseBody(String body) {
        ObjectMapper mapper = new ObjectMapper();
        I b = null;
        try {
            b = mapper.readValue(body, endpoint.getInputType());
            logger.debug("Parsed body to {}", endpoint.getInputType());
        } catch (JsonProcessingException e) {
            logger.warn("Could not parse body, assuming invalid body");
            return null;
        }
        return b;
    }

    // #region add query
    /**
     * Add query
     * 
     * @param r Request to add query to
     */
    private void addQuery(Request<I> r) {
        logger.debug("Parsing query");
        if (!firstLine.resource().contains("?")) {
            logger.debug("No query in current request");
            r.setQuery(new HashMap<>());
            return;
        }

        Map<String, String> q = new HashMap<>();

        String queryString = firstLine.resource().substring(firstLine.resource().indexOf("?") + 1);
        logger.debug("Query string to parse: {}", queryString);
        String[] queries = queryString.split("&");
        for (var x : queries) {
            String[] current = x.split("=");
            if (current.length == 1)
                q.put(current[0], null);
            else
                q.put(current[0], current[1]);
        }

        r.setQuery(q);
        logger.debug("Analysed query");
    }

    // #region handle response
    /**
     * Handles the response to the client
     * 
     * @param r Response object used to send response to the client
     * @throws IOException If an exception occurred during handling and the error
     *                     handling failed as well
     */
    private void handleResponse(Response<O> r) throws IOException {
        logger.info("Sending response");
        HttpCode c = HttpCode.ofCode(r.getCode());
        logger.debug("Code {}", c);

        WritingUtils.write(writer, "" + firstLine.protocol() + " " + c.getCode() + " " + c.getInfo());

        logger.debug("Writing server headers");
        serverHeaders.entrySet().forEach(s -> {
            WritingUtils.write(writer, s.getKey() + ": " + s.getValue());
        });

        if (r.getCode() >= 200 && r.getCode() < 300) {
            if (!endpoint.getOutputType().equals(new TypeReference<Void>() {
            })) {
                String bodyString = null;
                if (r.getBody().getString() == null || r.getBody().getString().isBlank())
                    bodyString = parseResponseBody(r.getBody().getObject());
                else
                    bodyString = r.getBody().getString();

                if (bodyString == null || bodyString.isBlank()) {
                    logger.debug("No body found");
                    return;
                }

                WritingUtils.write(writer,
                        "Content-Length: " + bodyString.getBytes().length);
                WritingUtils.write(writer, "");
                WritingUtils.write(writer, bodyString);
            }
        } else {
            WritingUtils.write(writer, "Content-Length: " + c.getInfo().getBytes().length);
            WritingUtils.write(writer, "");
            WritingUtils.write(writer, c.getInfo());
        }

        logger.debug("Sent response");
    }

    // #region parse response body
    /**
     * Parses response object to string for writing the response
     * 
     * @param body Body to parse
     * @return String representation of the body
     */
    private String parseResponseBody(O body) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // #region error handling
    /**
     * Returns a 500 internal server error to the client
     * 
     * @throws IOException
     */
    private void errorHandling() throws IOException {
        WritingUtils.write(writer, "" + firstLine.protocol() + " 500 Internal Server Error");
    }

    // #region close
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            if (!socket.isClosed())
                socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            logger.error("Could not close socket", e);
        }

        active = false;
    }

    // #region of
    /**
     * Creates session with given parameters
     * 
     * @param <I>    Input body type
     * @param <O>    Output body type
     * @param socket Client socket
     * @param f      First line sent by client
     * @param e      Endpoint called by client
     * @return Instantiated session
     */
    public static <I, O> HttpSession<I, O> of(Socket socket, FirstLine f, Endpoint<I, O> e, BufferedReader r,
            PrintWriter w) {
        return new HttpSession<>(socket, f, e, r, w);
    }

    @Override
    public synchronized void addSubscribedServer(Server server) {
        subscribers.add(server);
    }
}
