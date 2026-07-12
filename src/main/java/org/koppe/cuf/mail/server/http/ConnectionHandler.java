package org.koppe.cuf.mail.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.common.security.TLSWrapper;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Handler for the initial connection. Wraps the socket if necessary, gets the
 * first line of the request etc.
 */
@RequiredArgsConstructor
public class ConnectionHandler {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);
    /**
     * Server initialising this handler
     */
    private final HttpServer server;
    /**
     * Reader opened on the socket
     */
    @Getter
    private BufferedReader reader;
    /**
     * Writer opened on the socket
     */
    @Getter
    private PrintWriter writer;
    /**
     * First line of the request
     */
    @Getter
    private FirstLine firstLine = null;
    /**
     * Singnalises success of handling the connection
     */
    @Getter
    private boolean successful = true;
    /**
     * 
     */
    @Getter
    private Socket socket;
    /**
     * Endpoint the
     */
    @Getter
    private Endpoint<?, ?> endpoint;

    // #region handle
    /**
     * Main method for handling incoming connections
     */
    public void handle(Socket s) {
        socket = s;
        logger.debug("Starting session build up for incoming connection");
        if (server.isUseTls()) {
            logger.debug("Server uses tls, wrapping socket");
            if ((socket = TLSWrapper.wrapTls(socket, true)) == null) {
                logger.error("Could not wrap socket in tls");
                errorHandling();
                return;
            }
        }

        if (!initReaderAndWriter(socket)) {
            logger.error("Reader and writer could not be initalized");
            errorHandling();
            return;
        }

        readFirstLine();
        if (firstLine == null) {
            logger.error("No first line received from client");
            errorHandling();
            return;
        }

        endpoint = getEndpoint(firstLine);
        if (endpoint == null) {
            logger.error("Requested resource does not exist");
            notFound();
            errorHandling();
            return;
        }
        logger.info("Successfully handled connection");
        return;
    }

    // #region init reader and writer
    /**
     * Initialises reader and writer for the socket
     * 
     * @param socket Socket to initialise reader and writer for.
     * @return True, if they could be initialised, false otherwise
     */
    private boolean initReaderAndWriter(Socket socket) {
        logger.debug("Initialising reader and writer");
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            logger.error("Exception occurred while initialising reader and writer", e);
            return false;
        }
    }

    private void notFound() {
        WritingUtils.write(writer, "" + firstLine.protocol() + " 404 Not Found");
    }

    // #region read first line
    /**
     * Analyses the first line the client sent to initialize the session
     * 
     * @param socket Client socekt
     * @throws IOException If reader could not read
     */
    private void readFirstLine() {
        logger.debug("Reading first request line");
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            logger.error("Could not read first line", e);
            return;
        }

        logger.debug("Call to {}", line);

        String[] lineParts = line.split(" ");
        Method m = Method.ofValue(lineParts[0]);
        String resource = lineParts[1];
        String protocol = lineParts[2];

        firstLine = new FirstLine(resource, m, protocol);
    }

    private void errorHandling() {
        logger.warn("Error occurred in connection handling");
        logger.debug("Closing all open resources");
        successful = false;
        try {
            if (socket != null)
                socket.close();
            if (reader != null)
                reader.close();
            if (writer != null)
                writer.close();
        } catch (Exception ex) {
            logger.error("Exception occurred while closing resources, resources might be dangling", ex);
            return;
        }
    }

    /**
     * Returns a new endpoint instance matching the requested resource
     * 
     * @param f First line of the request containing request resource
     * @return
     */
    private Endpoint<?, ?> getEndpoint(FirstLine f) {
        String temp = f.resource();

        if (f.resource().contains("?")) {
            temp = f.resource().substring(0, f.resource().indexOf("?"));
        }

        Endpoint<?, ?> e = null;
        for (var x : HttpServer.getEndpoints().keySet()) {
            if (x.matches(temp)) {
                if (HttpServer.getEndpoints().get(x).get(f.method()) != null) {
                    e = newInstance(HttpServer.getEndpoints().get(x).get(f.method()));
                    break;
                }
            }
        }
        return e;
    }

    // #region new instance
    /**
     * Creates new instance of the endpoint called by client
     * 
     * @param e Endpoint to create a new instance of
     * @return The instantiated endpoint
     */
    private Endpoint<?, ?> newInstance(Endpoint<?, ?> e) {
        try {
            return e.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e1) {
            logger.warn("Could not get a new instance of endpoint {}", e, e1);
            return null;
        }
    }
}
