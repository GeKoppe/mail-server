package org.koppe.cuf.mail.server.smtp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.SessionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public final class SmtpServer implements Server {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(SmtpServer.class);
    /**
     * Socket the server listens on
     */
    private ServerSocket serverSocket;
    /**
     * Boolean that signalises whether the overall system is still online
     */
    @Getter
    @Setter
    private volatile boolean running = true;
    private final String hostname;
    /**
     * List of active sessions
     */
    private final List<SessionEntry> activeSessions = new ArrayList<>();
    /**
     * Factory for creating virtual threads
     */
    private final ThreadFactory vThreadFactory = Thread.ofVirtual().factory();
    /**
     * Port the smtp server should be running on.
     */
    private final int port;

    // #region open socket
    /**
     * Opens a socket on the configured smtp ports
     * 
     * @throws StartupException If socket could not be opened
     */
    private void openSocket() throws StartupException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
        } catch (IOException e) {
            logger.error("Could not open socket for smtp server, startup failed.", e);
            throw new StartupException("Could not open socket for smtp", e);
        }
    }

    // #region loop
    /**
     * Main loop logic of the server
     */
    private void loop() {
        try {
            Socket socket = serverSocket.accept();
            logger.info("Connection accepted on socket {}", serverSocket);
            logger.debug("Initialising new stmp session for client socket");

            SmtpSession session = new SmtpSession(socket, hostname);
            Thread thread = vThreadFactory.newThread(session);
            logger.debug("Created new session and thread for incoming connection");

            synchronized (activeSessions) {
                activeSessions.add(new SessionEntry(thread, session));
            }
            logger.debug("Starting thread for smtp session");
            thread.start();
        } catch (SocketTimeoutException e) {
            // Expected behaviour to not get stuck in the accept() method.
        } catch (IOException e) {
            if (!running)
                return;
            logger.info("Could not accept the client connection due to an exception", e);
        }
    }

    // #region cleanup
    /**
     * Cleans up all threads and smtp sessions currently active
     */
    private void cleanup() {
        logger.info("Cleaning up smtp sessions");
        for (var x : activeSessions) {
            if (x.session().isActive())
                x.session().close();
            if (x.thread().isAlive()) {
                try {
                    x.thread().join(2000);
                } catch (InterruptedException e) {
                    x.thread().interrupt();
                }
            }
        }
    }

    // #region run
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            openSocket();
        } catch (StartupException e) {
            logger.warn("Exception at startup of server", e);
            throw new RuntimeException(e);
        }

        logger.info("Accepting incoming smtp connections on port {}", port);
        while (running) {
            loop();
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.warn("Exception occurred while closing the socket", e);
            return;
        }
        cleanup();
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            running = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, I> void notify(Event<T, I> event) {
        if (!(event.getCause() instanceof SmtpSession))
            return;
        if (event.getInformation() instanceof StatusChangeEvent x) {
            if (x.getInformation() == StatusChange.DONE) {
                SessionEntry toRemove = null;
                synchronized (activeSessions) {
                    for (var y : activeSessions) {
                        if (y.session().equals(event.getCause())) {
                            toRemove = y;
                            break;
                        }
                    }
                    activeSessions.remove(toRemove);
                }
            }
        }
    }

}
