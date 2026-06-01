package org.koppe.cuf.mail.server.smtp.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;

public class ErrorAction implements CommandAction<SmtpState, SmtpContext> {

    @Override
    public void apply(SmtpContext c)  {
        WritingUtils.write(c.getWriter(), "503 Bad sequence of commands");
        c.setState(SmtpState.CLIENT_ERROR);
    }

}
