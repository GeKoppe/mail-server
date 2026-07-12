package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.config.MailConfig;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;

/**
 * Action for client EHLO command. Notifies the client of the server
 * capabilities.
 */
public class EhloAction implements CommandAction<SmtpState, SmtpContext> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(SmtpContext c) {
        WritingUtils.write(c.getWriter(),
                "250-" + c.getHostname() + " Hello " + c.getArguments().get("clientName"));
        WritingUtils.write(c.getWriter(), "250-SIZE " + MailConfig.MAX_MAIL_SIZE);
        WritingUtils.write(c.getWriter(), "250-STARTTLS");
        WritingUtils.write(c.getWriter(), "250-AUTH LOGIN PLAIN");
        WritingUtils.write(c.getWriter(), "250 SMTPUTF8");
        c.setState(SmtpState.EHLO);
    }

}
