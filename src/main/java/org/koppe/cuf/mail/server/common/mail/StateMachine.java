package org.koppe.cuf.mail.server.common.mail;

import org.koppe.cuf.mail.server.common.Interceptor;

/**
 * Classes implementing this interface are handling communication in state-based
 * protocols like SMTP, IMAP etc.
 */
public interface StateMachine<S extends State, T extends Context<S, ? extends Command<S>>> extends Runnable {
    public void setRequestHandler(RequestHandler request);

    public boolean isActive();

    public void setContext(T context);

    public StateMachine<S, T> addInterceptor(Interceptor<S, ? extends Command<S>> interceptor);
}
