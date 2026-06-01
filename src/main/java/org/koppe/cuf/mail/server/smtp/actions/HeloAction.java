package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;

public class HeloAction implements CommandAction<SmtpState, SmtpContext> {
    @Override
    public void apply(SmtpContext c)  {
        WritingUtils.write(c.getWriter(),
                "250 " + c.getHostname() + " Hello " + c.getArguments().get("clientName"));
        c.setState(SmtpState.EHLO);
    }
}
