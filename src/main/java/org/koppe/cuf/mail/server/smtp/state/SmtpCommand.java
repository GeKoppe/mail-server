package org.koppe.cuf.mail.server.smtp.state;

import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.smtp.actions.ConnectedAction;
import org.koppe.cuf.mail.server.smtp.actions.DataAction;
import org.koppe.cuf.mail.server.smtp.actions.EhloAction;
import org.koppe.cuf.mail.server.smtp.actions.ErrorAction;
import org.koppe.cuf.mail.server.smtp.actions.HeloAction;
import org.koppe.cuf.mail.server.smtp.actions.MailAction;
import org.koppe.cuf.mail.server.smtp.actions.NoopAction;
import org.koppe.cuf.mail.server.smtp.actions.QuitAction;
import org.koppe.cuf.mail.server.smtp.actions.RcptAction;
import org.koppe.cuf.mail.server.smtp.actions.RsetAction;
import org.koppe.cuf.mail.server.smtp.actions.StartTlsAction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All existing smtp commands, including the handling implementation
 */
@RequiredArgsConstructor
public enum SmtpCommand implements Command<SmtpState> {
    /**
     * Connected command
     */
    CONNECTED("", new ConnectedAction()),
    /**
     * Client EHLO to acknowledge connection.
     */
    EHLO("EHLO", new EhloAction()),
    /**
     * Start tls action
     */
    STARTTLS("STARTTLS", new StartTlsAction()),
    /**
     * HELO command by the client to acknowledge the connection and start tls
     * wrapping.
     */
    HELO("HELO", new HeloAction()),
    /**
     * Set the mails sender
     */
    MAIL("MAIL", new MailAction()),

    /**
     * Set the mails recipient
     */
    RCPT("RCPT", new RcptAction()),
    /**
     * Send data block
     */
    DATA("DATA", new DataAction()),
    /**
     * Quit communication
     */
    QUIT("QUIT", new QuitAction()),
    /**
     * Reset communication
     */
    RSET("RSET", new RsetAction()),
    /**
     * No operation, waiting
     */
    NOOP("NOOP", new NoopAction()),
    /**
     * Client error
     */
    ERROR("ERROR", new ErrorAction());

    /**
     * String representation of the command
     */
    @Getter
    private final String value;

    /**
     * Action representation of the command
     */
    @Getter
    private final CommandAction<SmtpState, SmtpContext> action;

    /**
     * Returns the SmtpCommand corresponding with the string representation given in
     * value.
     * 
     * @param value String representation of the command.
     * @return The command corresponding with the string representation or ERROR, if
     *         no valid command has been found.
     */
    public static SmtpCommand ofValue(String value) {
        for (SmtpCommand c : values())
            if (c.getValue().equals(value))
                return c;

        return ERROR;
    }
}
