package org.koppe.cuf.mail.server.imap.state;

import java.util.List;

import org.koppe.cuf.mail.server.common.mail.State;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImapState implements State {
    /**
     * Client has just connected with the server
     */
    CONNECTED(0, List.of()),
    /**
     * CLient is not authenticated
     */
    NOT_AUTHENTICATED(1,
            List.of(ImapCommand.NOOP, ImapCommand.LOGIN, ImapCommand.LOGOUT, ImapCommand.CAPABILITY,
                    ImapCommand.AUTHENTICATE, ImapCommand.STARTTLS)),
    /**
     * Client is authenticated at the server
     */
    AUTHENTICATED(2, List.of()),
    /**
     * Client has selected an element
     */
    SELECTED(3, List.of()),
    /**
     * Client has logged out
     */
    LOGOUT(4, List.of()),
    /**
     * Communication is done
     */
    DONE(5, List.of()),
    /**
     * Client error in the communication
     */
    CLIENT_ERROR(-1, List.of()),
    /**
     * Error in the connection itself
     */
    CONNECTION_ERROR(-2, List.of()),
    /**
     * General error
     */
    ERROR(-3, List.of());

    /**
     * Value of the state
     */
    @Getter
    private final int value;
    /**
     * Allowed commands during the state
     */
    @Getter
    private final List<ImapCommand> allowedCommands;

    @Override
    public State getErrorState() {
        return ERROR;
    }

}
