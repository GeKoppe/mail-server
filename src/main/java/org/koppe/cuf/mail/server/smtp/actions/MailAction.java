package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailAction implements CommandAction<SmtpState, SmtpContext> {
    private final Logger logger = LoggerFactory.getLogger(MailAction.class);

    @Override
    public void apply(SmtpContext c) {
        logger.debug("Received mail command, adding sender");
        String from = c.getArguments().get("args");
        c.getMail().setFrom(from.substring(from.indexOf("<") + 1, from.indexOf(">")));
        WritingUtils.write(c.getWriter(), "250 OK");
        c.setState(SmtpState.MAIL_FROM_SET);
    }

}
