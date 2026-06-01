package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RcptAction implements CommandAction<SmtpState, SmtpContext> {
    private final Logger logger = LoggerFactory.getLogger(RcptAction.class);

    @Override
    public void apply(SmtpContext c) {
        logger.debug("Received rcpt command, adding sender");

        if (c.getMail() == null) {
            logger.warn("No mail object given in command arguments");
            c.setState(SmtpState.CONNECTION_ERROR);
        }

        String recipient = c.getArguments().get("args");
        recipient = recipient.substring(recipient.indexOf("<") + 1, recipient.indexOf(">"));
        if (!c.getMail().getTo().contains(recipient)) {
            c.getMail().getTo().add(recipient);
            logger.debug("Added recipient");
        }

        WritingUtils.write(c.getWriter(), "250 OK");
        c.setState(SmtpState.RCPT_SET);
    }

}
