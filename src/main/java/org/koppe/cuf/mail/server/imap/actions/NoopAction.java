package org.koppe.cuf.mail.server.imap.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imap version of the noop action. Does nothing, just responds with an OK
 * message.
 */
public class NoopAction implements CommandAction<ImapState, ImapContext> {
    private final Logger logger = LoggerFactory.getLogger(NoopAction.class);

    @Override
    public void apply(ImapContext c) {
        logger.info("Received NOOP command, doing nothing");
        WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " OK NOOP completed");
    }

}
