package org.koppe.cuf.mail.server.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Interceptor;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.common.mail.MailStore;
import org.koppe.cuf.mail.server.config.MailConfig;
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
    /**
     * State machine
     */
    private final SmtpStateMachine machine = new SmtpStateMachine();
    /**
     * List of all subscribed server
     */
    private final List<Server> subs = new ArrayList<>();

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
        context.setSocket(socket);

        return true;
    }

    /**
     * Saves mail to the system
     * 
     * @throws Exception
     * @throws IOException
     * @throws IllegalAccessError
     */
    private void saveMail() throws IllegalAccessError, IOException, Exception {
        logger.info("Saving mail");
        try (MailStore ms = new MailStore(context.getUser())) {
            ms.save(getMail());
        }
    }

    /**
     * Returns mail this session is working on
     * 
     * @return The mail this session is working on
     */
    public Mail getMail() {
        return context.getMail();
    }

    // #region run
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.debug("Starting session for http connection");
        if (!setup()) {
            logger.error("Could not setup the required reader and writer for the socket");
            return;
        }
        notifySubscribers(new StatusChangeEvent(this, StatusChange.START));

        logger.debug("Setting up state machine");
        machine.setContext(context);
        machine.setRequestHandler(new SmtpRequestHandler());

        logger.debug("Starting state machine");
        machine.run();
        logger.debug("State machine done processing");

        if (!context.getState().equals(SmtpState.DONE)) {
            notifySubscribers(new StatusChangeEvent(this, StatusChange.DONE));
            return;
        }

        if (context.getMail().getTo().stream().filter(s -> s != null && !s.isBlank() && s.contains("@"))
                .map(s -> s.substring(s.indexOf("@") + 1).toUpperCase())
                .filter(s -> MailConfig.DOMAINS.stream().map(x -> x.toUpperCase()).toList().contains(s)).toList()
                .size() == 0 && context.getUser() != null) {
            logger.debug("Relaying message");
            new SmtpSender(true).send(context.getMail());
        } else {
            logger.debug("Saving message");
            try {
                saveMail();
            } catch (IllegalAccessError | Exception e) {
                logger.info("Exception occurred while saving mail {}", getMail());
                return;
            }
        }
    }

    // #region close
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            socket.close();
            context.getReader().close();
            context.getWriter().close();
        } catch (IOException e) {
            logger.warn("Could not close socket due to an io exception", e);
        }
        context.setActive(false);
    }

    /**
     * Returns true, if session is still active
     */
    public boolean isActive() {
        return context.isActive();
    }

    /**
     * Adds an interceptor to the communication. Interceptor will intercept all
     * outgoing communication.
     * 
     * @param interceptor Interceptor to add to the session
     */
    public void addInterceptor(Interceptor<SmtpState, ? extends Command<SmtpState>> interceptor) {
        synchronized (machine) {
            machine.addInterceptor(interceptor);
        }
    }

    private void notifySubscribers(Event<Session, StatusChange> event) {
        synchronized (subs) {
            subs.forEach(s -> s.notify(event));
        }
    }

    @Override
    public void addSubscribedServer(Server server) {
        logger.debug("Adding server {} to subscribers");
        synchronized (subs) {
            subs.add(server);
        }
    }
}
