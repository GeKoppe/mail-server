package org.koppe.cuf.mail.server.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.SessionEntry;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
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
    @Getter
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
                vThreadFactory.newThread(() -> {
                    ConnectionHandler handler = new ConnectionHandler(this);
                    handler.handle(clientSocket);
                    if (!handler.isSuccessful()) {
                        logger.warn("Handler could not build connection to client");
                        return;
                    }
                    HttpSession<?, ?> session = HttpSession.of(clientSocket, handler.getFirstLine(),
                            handler.getEndpoint(),
                            handler.getReader(),
                            handler.getWriter());

                    session.addSubscribedServer(this);
                    logger.debug("Initialised session for client connection");

                    Thread thread = vThreadFactory.newThread(session);
                    logger.debug("Initialised thread for client session");

                    SessionEntry ent = new SessionEntry(thread, session);
                    synchronized (sessions) {
                        sessions.add(ent);
                    }
                    thread.start();

                    logger.info("Started thread for client connection");
                }).start();
            } catch (IOException e) {

            }
            cleanSessions();
        }
        logger.info("Server is about to shut down");
        endSessions();
        running = false;
    }

    // #region clean sessions
    /**
     * Clean sessions that have ended
     */
    private void cleanSessions() {
        logger.debug("Cleaning up sessions that have ended");
        List<SessionEntry> toRemove = new ArrayList<>();

        synchronized (sessions) {
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
        }
        logger.debug("{} sessions to clean up: {}", toRemove.size(), toRemove);
        toRemove.forEach(t -> removeSession(t));
    }

    /**
     * Synchronized implementation of removing sessions
     * 
     * @param e Session to remove
     */
    private void removeSession(SessionEntry e) {
        synchronized (sessions) {
            sessions.remove(e);
        }
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
    private void endSessions() {
        logger.debug("Trying to gracefully shut down sessions");
        synchronized (sessions) {
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
    }

    // #region shutdown
    /**
     * {@inheritDoc}
     */
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
            if (endpoints.get(endpoint.getPath()).get(endpoint.getMethod()) == endpoint)
                return;
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
     * Returns a map of all endpoints known to the server
     * 
     * @return map of all endpoints known to the server
     */
    public static Map<Path, Map<Method, Endpoint<?, ?>>> getEndpoints() {
        return endpoints;
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
