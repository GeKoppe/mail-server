package org.koppe.cuf.mail.server.imap.state;

import java.io.BufferedReader;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

import org.koppe.cuf.mail.server.common.mail.Context;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.db.jpa.BoxElement;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.service.FolderService;
import org.koppe.cuf.mail.server.db.service.MailService;
import org.koppe.cuf.mail.server.db.service.UserService;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ImapContext implements Context<ImapState, ImapCommand> {
    /**
     * Current state
     */
    @Setter
    private volatile ImapState state;
    /**
     * Is the connection active?
     */
    @Setter
    private volatile boolean active;
    /**
     * Reader for the input stream of the socket
     */
    @Setter
    private BufferedReader reader;
    /**
     * Writer for the output stream of the socket
     */
    @Setter
    private Writer writer;
    /**
     * Hostname of the machine
     */
    @Setter
    private String hostname;
    /**
     * Current arguments sent by the host
     */
    @Setter
    private Map<String, String> arguments;
    /**
     * Socket of the connection
     */
    @Setter
    private Socket socket;
    @Setter
    private boolean authenticated;
    /**
     * Service for fetching mail information from the databse
     */
    private final MailService mailService = new MailService();
    /**
     * Service for fetching user information from the databse
     */
    private final UserService userService = new UserService();
    /**
     * Service for fetching folder information from the databse
     */
    private final FolderService folderService = new FolderService();
    /**
     * Logged on user
     */
    @Setter
    private User user;
    /**
     * Number of errors the client has already made
     */
    private int clientErrors = 0;
    /**
     * Command the client has sent.
     */
    @Setter
    private ImapCommand clientCommand;
    /**
     * Element that is currently held by the session
     */
    @Setter
    private BoxElement currentElement = null;
    /**
     * If true, current element can be modified
     */
    @Setter
    private boolean write = false;

    @Override
    public Mail getMail() {
        throw new UnsupportedOperationException("Unimplemented method 'getMail'");
    }

    @Override
    public void setMail(Mail mail) {
        throw new UnsupportedOperationException("Unimplemented method 'setMail'");
    }

    public void incClientErrors() {
        clientErrors++;
    }

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }

}
