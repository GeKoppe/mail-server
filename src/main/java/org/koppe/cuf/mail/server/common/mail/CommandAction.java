package org.koppe.cuf.mail.server.common.mail;

/**
 * Represents an action that is to be executed, if a client sends the
 * corresponding command
 */
@FunctionalInterface
public interface CommandAction<T extends State, C extends Context<T, ? extends Command<T>>> {
    /**
     * Execution of the action
     * 
     * @param c Current communication context
     */
    public void apply(C c);
}
