package org.koppe.cuf.mail.server.smtp.state;

import java.util.List;

import org.koppe.cuf.mail.server.common.mail.State;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * All SMTP protocol states and the allowed commands per state.
 */
@RequiredArgsConstructor
@ToString
public enum SmtpState implements State {
    /**
     * Client has connected to the smtp server
     */
    CONNECTED(0, List.of(SmtpCommand.CONNECTED)),
    /**
     * Server has greeted the client
     */
    GREETED(1, List.of(SmtpCommand.HELO, SmtpCommand.EHLO, SmtpCommand.QUIT, SmtpCommand.NOOP)),
    /**
     * EHLO or HELO sent by client
     */
    EHLO(2, List.of(SmtpCommand.MAIL, SmtpCommand.STARTTLS, SmtpCommand.QUIT, SmtpCommand.RSET, SmtpCommand.NOOP,
            SmtpCommand.EHLO, SmtpCommand.HELO)),
    /**
     * Mail from has been set
     */
    MAIL_FROM_SET(3, List.of(SmtpCommand.RCPT, SmtpCommand.QUIT, SmtpCommand.RSET, SmtpCommand.NOOP)),
    /**
     * Recipient has been set
     */
    RCPT_SET(4, List.of(SmtpCommand.RCPT, SmtpCommand.DATA, SmtpCommand.QUIT, SmtpCommand.RSET, SmtpCommand.NOOP)),
    /**
     * Data has been sent
     */
    DATA(5, List.of()),
    /**
     * Communication is done
     */
    DONE(6, List.of()),
    /**
     * Signalises an error has occurred in state processing
     */
    CLIENT_ERROR(-1, List.of()),
    /**
     * Signalises an error has occurred in the connection
     */
    CONNECTION_ERROR(-2, List.of());

    @Getter
    private final int value;

    @Getter
    private final List<SmtpCommand> allowedCommands;

    public static SmtpState ofValue(int value) {
        return switch (value) {
            case 0 -> CONNECTED;
            case 1 -> GREETED;
            case 2 -> EHLO;
            case 3 -> MAIL_FROM_SET;
            case 4 -> RCPT_SET;
            case 5 -> DATA;
            case 6 -> DONE;
            case -1 -> CLIENT_ERROR;
            case -2 -> CONNECTION_ERROR;
            default -> CONNECTION_ERROR;
        };
    }

    @Override
    public SmtpState getErrorState() {
        return CLIENT_ERROR;
    }

}
