package org.koppe.cuf.mail.server.imap.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that is executed during logout
 */
public class LogoutAction implements CommandAction<ImapState, ImapContext> {
    private final Logger logger = LoggerFactory.getLogger(LogoutAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(ImapContext c) {
        logger.info("Client is logging out of session");
        WritingUtils.write(c.getWriter(), "* Logging out");
        WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " OK LOGOUT completed");
        c.setState(ImapState.LOGOUT);
        logger.info("Logout done");
        return;
    }

}
