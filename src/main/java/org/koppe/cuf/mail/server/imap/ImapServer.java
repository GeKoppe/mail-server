package org.koppe.cuf.mail.server.imap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.SessionEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImapServer implements Server {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(ImapServer.class);
    /**
     * Port
     */
    private final int port;
    /**
     * Hostname
     */
    private final String hostname;
    /**
     * Signalises whether the system is still running
     */
    private volatile boolean running = true;
    /**
     * Socket on which to accept incoming imap connections
     */
    private ServerSocket serverSocket;
    /**
     * Factory for virtual threads
     */
    private final ThreadFactory vThreadFactory = Thread.ofVirtual().factory();
    /**
     * Active imap sessions
     */
    private final List<SessionEntry> activeSessions = new ArrayList<>();

    // #region setup
    /**
     * Setup for the imap server. Initialises server socket.
     * 
     * @throws StartupException If bind to socket failed
     */
    private void setup() throws StartupException {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);
            logger.debug("Bound to socket {}", serverSocket);
        } catch (IOException e) {
            logger.error("Could not bind to socket due to IOException", e);
            throw new StartupException("Failed to bind to socket", e);
        }
    }

    // #region loop
    /**
     * Server accept loop. Waits for incoming connections
     */
    private void loop() {
        try {
            Socket socket = serverSocket.accept();
            logger.info("Incoming connection on socket {}", serverSocket);

            ImapSession session = new ImapSession(hostname, socket);
            logger.debug("Initialised session for incoming imap connection");

            Thread thread = vThreadFactory.newThread(session);
            logger.debug("Initialised virtual thread for imap session");

            synchronized (activeSessions) {
                activeSessions.add(new SessionEntry(thread, session));
            }

            logger.debug("Starting imap session thread");
            thread.start();
        } catch (SocketTimeoutException ex) {
            // Expected behaviour to break out of socket.accept
        } catch (IOException e) {
            logger.warn("Connection exception occurred", e);
            return;
        }
    }

    // #region run
    /**
     * 
     */
    @Override
    public void run() {
        logger.info("Starting up imap server");
        try {
            setup();
        } catch (StartupException e) {
            return;
        }

        logger.info("Accepting incoming imap connections on port {}", port);
        while (running) {
            loop();
        }

    }

    @Override
    public void shutdown() {

    }

    @Override
    public <T, I> void notify(Event<T, I> event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notify'");
    }

}
