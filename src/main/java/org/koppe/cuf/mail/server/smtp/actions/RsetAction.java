package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RsetAction implements CommandAction<SmtpState, SmtpContext> {
    private final Logger logger = LoggerFactory.getLogger(RsetAction.class);

    @Override
    public void apply(SmtpContext c) {
        logger.debug("Client wants to reset connection");
        c.setMail(new Mail());
        WritingUtils.write(c.getWriter(), "250 OK");
        c.setState(SmtpState.EHLO);
    }

}
