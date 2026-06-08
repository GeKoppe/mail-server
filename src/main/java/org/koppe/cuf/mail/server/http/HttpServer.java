package org.koppe.cuf.mail.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLSocket;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.TLSContext;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.SessionEntry;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

/**
 * Main component for http communication handling
 */
@RequiredArgsConstructor
public class HttpServer implements Server {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    /**
     * Port the server is running on
     */
    private final int port;
    /**
     * True, if tls is to be used
     */
    private final boolean useTls;
    /**
     * Server socket
     */
    private ServerSocket socket;
    /**
     * Signalises whether the server is running
     */
    private volatile boolean running = false;
    /**
     * List of all current sessions
     */
    private final List<SessionEntry> sessions = new ArrayList<>();
    /**
     * Thread factory
     */
    private final ThreadFactory vThreadFactory = Thread.ofVirtual().factory();
    /**
     * List of all known endpoints
     */
    private static Map<Path, Map<Method, Endpoint<?, ?>>> endpoints = new HashMap<>();

    // #region setup
    /**
     * Opens the socket
     * 
     * @throws StartupException If opening the socket failed
     */
    private void setup() throws StartupException {
        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(10000);
        } catch (IOException e) {
            logger.error("Exception occurred on startup of http server", e);
            throw new StartupException("Exception occurred during startup of http server", e);
        }

        running = true;
    }

    // #region run
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.info("Starting http server");
        try {
            setup();
        } catch (StartupException e) {
            logger.error("Server could not be started", e);
        }

        logger.debug("Accepting connections on port {}", port);
        while (running) {
            try {
                Socket clientSocket = socket.accept();
                logger.info("New client connection on {}", clientSocket);
                if (useTls) {
                    logger.debug("Wrapping tls");
                    Socket tls = wrapTls(clientSocket);
                    if (tls == null) {
                        logger.error("Fatal error, could not build ssl socket for client connection");
                        break;
                    }
                    clientSocket = tls;
                }

                FirstLine firstLine = null;
                BufferedReader reader = null;
                PrintWriter writer = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new PrintWriter(clientSocket.getOutputStream());
                } catch (Exception ex) {
                    logger.error("Could not initialize stream reader and writer");
                    if (reader != null)
                        reader.close();
                    if (writer != null)
                        writer.close();

                    clientSocket.close();
                    continue;
                }

                try {
                    firstLine = getFirstLine(reader);
                } catch (IOException ex) {
                    logger.error("First line could not be analysed");
                    reader.close();
                    writer.close();
                    clientSocket.close();
                    cleanSessions();
                    continue;
                }

                if (firstLine == null) {
                    errorHandling(writer);
                    reader.close();
                    writer.close();
                    clientSocket.close();
                    cleanSessions();
                    continue;
                }
                Endpoint<?, ?> e = getEndpoint(firstLine);
                if (e == null) {
                    logger.warn("Endpoint for given resource and method not found");
                    notFound(writer, firstLine);
                    writer.close();
                    reader.close();
                    clientSocket.close();
                    cleanSessions();
                    continue;
                }
                HttpSession<?, ?> session = HttpSession.of(clientSocket, firstLine, e, reader,
                        writer);

                session.addSubscribedServer(this);
                logger.debug("Initialised session for client connection");

                Thread thread = vThreadFactory.newThread(session);
                logger.debug("Initialised thread for client session");

                SessionEntry ent = new SessionEntry(thread, session);
                sessions.add(ent);
                thread.start();

                logger.info("Started thread for client connection");
            } catch (IOException e) {

            }
            cleanSessions();
        }
        logger.info("Server is about to shut down");
        endSessions();
        running = false;
    }

    private Socket wrapTls(Socket socket) {
        try {
            SSLSocket tls = (SSLSocket) TLSContext.getInstance().getSocketFactory().createSocket(socket,
                    socket.getInputStream(),
                    true);
            tls.setUseClientMode(false);
            tls.startHandshake();
            return tls;
        } catch (IOException | StartupException e) {
            logger.error("Could not build tls socket due to exception", e);
            return null;
        }
    }

    // #region error handling
    /**
     * Handles errors
     * 
     * @param socket Socket of the connection
     */
    private void errorHandling(PrintWriter w) {
        return;
    }

    private void notFound(PrintWriter w, FirstLine f) {
        WritingUtils.write(w, "" + f.protocol() + " 404 Not Found");
    }

    // #region get first line
    /**
     * Analyses the first line the client sent to initialize the session
     * 
     * @param socket Client socekt
     * @return Representation of first http line
     * @throws IOException If reader could not read
     */
    private FirstLine getFirstLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        logger.debug("Call to {}", line);

        String[] lineParts = line.split(" ");
        Method m = Method.ofValue(lineParts[0]);
        String resource = lineParts[1];
        String protocol = lineParts[2];

        return new FirstLine(resource, m, protocol);
    }

    protected Endpoint<?, ?> getEndpoint(FirstLine f) {
        String temp = f.resource();

        if (f.resource().contains("?")) {
            temp = f.resource().substring(0, f.resource().indexOf("?"));
        }

        Endpoint<?, ?> e = null;
        for (var x : endpoints.keySet()) {
            if (x.matches(temp)) {
                if (endpoints.get(x).get(f.method()) != null) {
                    e = newInstance(endpoints.get(x).get(f.method()));
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

    // #region clean sessions
    /**
     * Clean sessions that have ended
     */
    private void cleanSessions() {
        logger.debug("Cleaning up sessions that have ended");
        List<SessionEntry> toRemove = new ArrayList<>();

        sessions.forEach(s -> {
            if (s.session().isActive())
                return;
            logger.debug("Session {} has ended, cleaning up", s);

            try {
                if (s.thread().isAlive())
                    s.thread().join();
            } catch (InterruptedException ex) {
                logger.warn("Could not join thread {}", s.thread(), ex);
                return;
            }
            toRemove.add(s);
        });
        logger.debug("{} sessions to clean up: {}", toRemove.size(), toRemove);
        toRemove.forEach(t -> removeSession(t));
    }

    /**
     * Synchronized implementation of removing sessions
     * 
     * @param e Session to remove
     */
    private synchronized void removeSession(SessionEntry e) {
        sessions.remove(e);
    }

    /**
     * Removes session from list of all sessions
     * 
     * @param s Session to remove
     */
    private void removeSession(Session s) {
        SessionEntry toRemove = null;
        for (var x : sessions) {
            if (x.session().equals(s)) {
                toRemove = x;
                break;
            }
        }

        if (toRemove != null) {
            removeSession(toRemove);
        }
    }

    // #region end sessions
    /**
     * Ends all sessions at shutdown
     */
    private synchronized void endSessions() {
        logger.debug("Trying to gracefully shut down sessions");
        sessions.forEach(s -> {
            logger.debug("Ending session {}", s);
            try {
                if (s.thread().isAlive())
                    s.thread().join();

                logger.debug("Ended thread {}", s.thread());
            } catch (InterruptedException e) {
                logger.warn("Could not join thread {}", s.thread(), e);
            }
        });
    }

    @Override
    public void shutdown() {
        running = false;
        try {
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            logger.warn("Could not close socket {}", socket);
        } catch (NullPointerException ex) {
            logger.error("How did this even happen?");
        }
    }

    // #region register endpoint
    /**
     * Registers an endpoint at the server. Endpoint will be exposed.
     * 
     * @param <I>      Input body type of the endpoint
     * @param <O>      Output body type of the endpoint
     * @param endpoint Endpoint to register
     * @throws StartupException If an endpoint with idential path and method already
     *                          exists
     */
    public static <I, O> void registerEndpoint(Endpoint<I, O> endpoint) throws StartupException {
        Logger logger = LoggerFactory.getLogger(HttpServer.class);
        if (!endpoints.containsKey(endpoint.getPath()))
            endpoints.put(endpoint.getPath(), new HashMap<>());

        if (endpoints.get(endpoint.getPath()).containsKey(endpoint.getMethod())) {
            logger.error("Could not register endpoint {}, combination of path and method already exists on endpoint {}",
                    endpoint, endpoints.get(endpoint.getPath()).get(endpoint.getMethod()));
            throw new StartupException(
                    String.format("Could not load all endpoints, duplicate path and method for %s and %s",
                            endpoint, endpoints.get(endpoint.getPath()).get(endpoint.getMethod())),
                    null);
        }

        endpoints.get(endpoint.getPath()).put(endpoint.getMethod(), endpoint);
    }

    /**
     * 
     */
    @Override
    public synchronized <T, I> void notify(Event<T, I> event) {
        logger.debug("Caught event {}", event);
        if (event instanceof StatusChangeEvent e) {
            if (e.getInformation() == StatusChange.DONE) {
                removeSession(e.getCause());
            }
        }
    }
}
