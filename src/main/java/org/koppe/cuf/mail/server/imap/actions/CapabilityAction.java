package org.koppe.cuf.mail.server.imap.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapabilityAction implements CommandAction<ImapState, ImapContext> {
    private final Logger logger = LoggerFactory.getLogger(CapabilityAction.class);

    @Override
    public void apply(ImapContext c) {
        logger.debug("Answering capabilities command");
        WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " OK [CAPABILITES EXIST]");
    }

}
