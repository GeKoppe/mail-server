package org.koppe.cuf.mail.server.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.koppe.cuf.mail.server.common.Interceptor;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpRequestHandler;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.koppe.cuf.mail.server.smtp.state.SmtpStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

/**
 * Smtp session handler
 */
@RequiredArgsConstructor
public class SmtpSession implements Session {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(SmtpSession.class);
    /**
     * Session Socket
     */
    private final Socket socket;
    /**
     * Hostname
     */
    private final String hostname;
    /**
     * Smtp context
     */
    private final SmtpContext context = new SmtpContext();
    private final SmtpStateMachine machine = new SmtpStateMachine();

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
        context.setState(SmtpState.CONNECTED);
        context.setActive(true);

        return true;
    }

    private void saveMail() {
        logger.info("Mail: {}", context.getMail());
    }

    public Mail getMail() {
        return context.getMail();
    }

    // #region run
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        if (!setup()) {
            logger.error("Could not setup the required reader and writer for the socket");
            return;
        }

        machine.setContext(context);
        machine.setRequestHandler(new SmtpRequestHandler());
        machine.run();

        if (context.getState().equals(SmtpState.DONE))
            saveMail();
    }

    // #region close
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Could not close socket due to an io exception", e);
        }
        context.setActive(false);
    }

    public boolean isActive() {
        return context.isActive();
    }

    public void addInterceptor(Interceptor<SmtpState, ? extends Command<SmtpState>> interceptor) {
        machine.addInterceptor(interceptor);
    }

    @Override
    public void addSubscribedServer(Server server) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addSubscribedServer'");
    }
}
