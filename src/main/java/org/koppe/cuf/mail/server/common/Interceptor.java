package org.koppe.cuf.mail.server.common;

import org.koppe.cuf.mail.server.common.exceptions.InterceptException;
import org.koppe.cuf.mail.server.common.mail.Command;
import org.koppe.cuf.mail.server.common.mail.Context;
import org.koppe.cuf.mail.server.common.mail.State;

/**
 * Classes implementing this interface can intercept communication on sockets.
 * Interceptors should never force a client to send a new command, they should
 * only be used to add to the communication. If, for example, a client has sent
 * a LOGIN command, an interceptor might do pre authentication checks like if
 * the client ip is white-listed etc., can change states and so on but it should
 * never actually handle the login attempt as that might interrupt the
 * {@link org.koppe.cuf.mail.server.common.mail.StateMachine} execution.
 */
public interface Interceptor<T extends State, C extends Command<T>> {
    // #region intercept
    /**
     * Intercepts communication.
     * 
     * @param context Context of the communication to be intercepted
     * @return The intercepted communication
     */
    public void intercept(Context<T, ? extends Command<T>> context) throws InterceptException;

    // #region get command
    /**
     * Returns the command this client should intercept
     * 
     * @return The command this interceptor should intercept
     */
    public C getCommand();

    // #region get state
    /**
     * Returns the state this interceptor should intercept
     * 
     * @return state this interceptor should intercept
     */
    public T getState();
}
