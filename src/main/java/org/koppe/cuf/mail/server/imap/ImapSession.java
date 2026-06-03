package org.koppe.cuf.mail.server.imap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapRequestHandler;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.koppe.cuf.mail.server.imap.state.ImapStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImapSession implements Session {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(ImapSession.class);
    /**
     * Hostname of the server
     */
    private final String hostname;
    /**
     * Socket of the connection
     */
    private final Socket socket;
    /**
     * Context
     */
    private volatile ImapContext context = new ImapContext();

    // #region setup
    /**
     * Sets up the read and write fields for the session
     * 
     * @return True, if setup was successful, false otherwise
     */
    private boolean setup() {
        try {
            context.setReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        } catch (IOException e) {
            logger.warn("Could not get input stream from socket {}", socket, e);
            return false;
        }

        try {
            context.setWriter(new PrintWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.warn("Could not get socket output stream {}", socket, e);
            return false;
        }

        logger.debug("Successfully set up reader and writer for the smtp session");

        context.setMail(new Mail());
        context.setHostname(hostname);
        context.setState(ImapState.CONNECTED);

        return true;
    }

    // #region run
    /**
     * {@inheritDoc}
     * Handles the client connection and all requests
     */
    @Override
    public void run() {
        logger.info("Handling client connection on socket {}", socket);
        if (!setup()) {
            logger.warn("Setup of required tools for handling the client connection failed");
            close();
            return;
        }

        ImapStateMachine m = new ImapStateMachine();
        m.setContext(context);
        m.setRequestHandler(new ImapRequestHandler());
        m.run();
    }

    // #region close
    /**
     * {@inheritDoc}
     * Closes the client socket
     */
    @Override
    public void close() {
        context.setActive(false);
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Could not close socket {}", socket);
        }
    }

    @Override
    public boolean isActive() {
        return context.isActive();
    }

    @Override
    public void addSubscribedServer(Server server) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addSubscribedServer'");
    }

}
