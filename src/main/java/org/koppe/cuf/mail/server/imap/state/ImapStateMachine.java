package org.koppe.cuf.mail.server.imap.state;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.koppe.cuf.mail.server.common.Interceptor;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Request;
import org.koppe.cuf.mail.server.common.mail.RequestHandler;
import org.koppe.cuf.mail.server.common.mail.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

public class ImapStateMachine implements StateMachine<ImapState, ImapContext> {
    private final Logger logger = LoggerFactory.getLogger(ImapStateMachine.class);
    private RequestHandler handler;
    @Setter
    private ImapContext context;
    private final Map<ImapState, List<Interceptor<ImapState, ? extends Command<ImapState>>>> interceptors = new HashMap<>();

    @Override
    public void run() {
        logger.info("Handling communication on socket {}");
        Request request = handler.read(context.getReader());
        request.arguments();
    }

    @Override
    public void setRequestHandler(RequestHandler request) {
        handler = request;
    }

    @Override
    public boolean isActive() {
        return context.isActive();
    }

    @Override
    public StateMachine<ImapState, ImapContext> addInterceptor(
            Interceptor<ImapState, ? extends Command<ImapState>> interceptor) {
        if (interceptors.get(interceptor.getState()) == null)
            interceptors.put(interceptor.getState(), new LinkedList<>());

        interceptors.get(interceptor.getState()).add(interceptor);
        return this;
    }

}
