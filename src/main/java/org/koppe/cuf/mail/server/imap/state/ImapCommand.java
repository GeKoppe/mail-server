package org.koppe.cuf.mail.server.imap.state;

import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.imap.actions.*;
import org.koppe.cuf.mail.server.imap.actions.mailbox.*;
import org.koppe.cuf.mail.server.imap.actions.msg.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImapCommand implements Command<ImapState> {
    /**
     * No operation
     */
    NOOP("NOOP", new NoopAction()),
    /**
     * Capabilities of the server
     */
    CAPABILITY("CAPABILITY", new CapabilityAction()),
    /**
     * Log out of the session
     */
    LOGOUT("LOGOUT", new LogoutAction()),
    /**
     * Wrap socket in tls
     */
    STARTTLS("STARTTLS", new StarttlsAction()),
    /**
     * Authenticate
     */
    AUTHENTICATE("AUTHENTICATE", new AuthenticateAction()),
    /**
     * Login
     */
    LOGIN("LOGIN", new LoginAction()),
    /**
     * List items in selected folder
     */
    LIST("LIST", new ListAction()),
    /**
     * I really don't know
     */
    LSUB("LSUB", new LsubAction()),
    /**
     * Creates a new element
     */
    CREATE("CREATE", new CreateAction()),
    /**
     * Deletes an element
     */
    DELETE("DELETE", new DeleteAction()),
    /**
     * Renames an element
     */
    RENAME("RENAME", new RenameAction()),
    SUBSCRIBE("SUBSCRIBE", new SubscribeAction()),
    UNSUBSCRIBE("UNSUBSCRIBE", new UnsubscribeAction()),
    STATUS("STATUS", new StatusAction()),
    SELECT("SELECT", new SelectAction()),
    EXAMINE("EXAMINE", new ExamineAction()),
    FETCH("FETCH", new FetchAction()),
    FETCH_UID("UID FETCH", new FetchUidAction()),
    SEARCH("SEARCH", new SearchAction()),
    UNSELECT("UNSELECT", new UnselectAction()),
    ERROR("", new ErrorAction()),
    CLOSE("CLOSE", new CloseAction()),
    EXPUNGE("EXPUNGE", new ExpungeAction()),
    SEARCH_UID("UID SEARCH", new ListAction()),
    APPEND("APPEND", new AppendAction()),
    COPY("COPY", new CopyAction()),
    STORE("STORE", new StoreAction()),
    STORE_UID("UID STORE", new StoreUidAction());

    @Getter
    private final String value;
    @Getter
    private final CommandAction<ImapState, ImapContext> action;

    public static ImapCommand ofValue(String value) {
        for (ImapCommand comm : values())
            if (comm.getValue().equals(value))
                return comm;

        return ERROR;
    }
}
