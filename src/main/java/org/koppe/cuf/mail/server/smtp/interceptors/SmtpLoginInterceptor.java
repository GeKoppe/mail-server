package org.koppe.cuf.mail.server.smtp.interceptors;

import org.koppe.cuf.mail.server.common.Interceptor;
import org.koppe.cuf.mail.server.common.exceptions.InterceptException;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Context;
import org.koppe.cuf.mail.server.smtp.state.SmtpCommand;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;

import lombok.Getter;

/**
 * Does pre authentication checks. If those checks fail, sets the state to
 * client error.
 */
public class SmtpLoginInterceptor implements Interceptor<SmtpState, SmtpCommand> {
    /**
     * Command the interceptor should intercept
     */
    @Getter
    private final SmtpCommand command = SmtpCommand.EHLO;
    /**
     * State the interceptor should intercept
     */
    @Getter
    private final SmtpState state = SmtpState.GREETED;

    /**
     * {@inheritDoc}
     */
    @Override
    public void intercept(Context<SmtpState, ? extends Command<SmtpState>> context) throws InterceptException {
        return;
    }

}
