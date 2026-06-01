package org.koppe.cuf.mail.server.smtp.state;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.koppe.cuf.mail.server.common.Interceptor;
import org.koppe.cuf.mail.server.common.exceptions.InterceptException;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Request;
import org.koppe.cuf.mail.server.common.mail.RequestHandler;
import org.koppe.cuf.mail.server.common.mail.State;
import org.koppe.cuf.mail.server.common.mail.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class SmtpStateMachine implements StateMachine<SmtpState, SmtpContext> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(SmtpStateMachine.class);
    /**
     * Request handler
     */
    @Setter
    private RequestHandler requestHandler;
    /**
     * Context of the communication
     */
    @Getter
    @Setter
    private SmtpContext context;
    /**
     * Map of all interceptors sorted by state they should intercept
     */
    private final Map<SmtpState, List<Interceptor<SmtpState, ? extends Command<SmtpState>>>> interceptors = new HashMap<>();

    // #region run
    /**
     * {@inheritDoc}
     * Executes the state machine logic. Lets the request handler parse the client
     * request, calls interceptors for the request and calls the command of the
     * request afterwards.
     */
    @Override
    public void run() {
        // Continuously loop through the states.
        while (context.isActive() && context.getState().getValue() >= SmtpState.CONNECTED.getValue()
                && context.getState().getValue() < SmtpState.DONE.getValue()) {
            logger.info("Current state: {}. Handling request", context.getState());

            // If state is connected, send greeting
            if (context.getState() == SmtpState.CONNECTED) {
                // Call all interceptors for connected state
                executeInterceptors(SmtpState.CONNECTED, SmtpCommand.CONNECTED);
                SmtpCommand.CONNECTED.getAction().apply(context);
                continue;
            }

            // Parse the request
            Request request = requestHandler.read(context.getReader());

            if (request == null || request.command() == null) {
                logger.error("Could not parse request");
                context.setState(SmtpState.CONNECTION_ERROR);
                context.setClientCommand(SmtpCommand.ERROR);
                continue;
            }

            // Check if command is valid for current state
            if (!State.validCommand(context.getState(), request.command())) {
                logger.warn("Invalid command for current state");
                SmtpCommand.ERROR.getAction().apply(context);
                continue;
            }

            // Set arguments and call all relevant interceptors for current state
            context.setArguments(request.arguments());
            context.setClientCommand((SmtpCommand) request.command());
            executeInterceptors(context.getState(), (SmtpCommand) request.command());

            // Check command again, as interceptors might have changed the state
            if (!State.validCommand(context.getState(), request.command())) {
                logger.warn("Invalid command for current state");
                SmtpCommand.ERROR.getAction().apply(context);
                continue;
            }

            // Execute command
            logger.debug("Executing command {}", request.command());
            ((SmtpCommand) request.command()).getAction().apply(context);
            logger.info("New state after execution: {}", context.getState());
        }
        logger.info("Exiting with state {}", context.getState());
        context.setActive(false);
    }

    /**
     * Executes all interceptors for current state and command
     * 
     * @param state   State to be intercepted
     * @param command Command to be intercepted
     */
    private void executeInterceptors(SmtpState state, SmtpCommand command) {
        interceptors.computeIfPresent(state, (s, l) -> {
            l.forEach(i -> {
                if (i.getCommand().equals(command))
                    try {
                        i.intercept(context);
                    } catch (InterceptException e) {
                        logger.info("Interception on interceptor {} failed", i, e);
                    }
            });
            return l;
        });
    }

    // #region is active
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return context.isActive();
    }

    // #region add interceptor
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized SmtpStateMachine addInterceptor(
            Interceptor<SmtpState, ? extends Command<SmtpState>> interceptor) {
        if (interceptors.get(interceptor.getState()) == null)
            interceptors.put(interceptor.getState(), new LinkedList<>());
        interceptors.get(interceptor.getState()).add(interceptor);
        return this;
    }

}
