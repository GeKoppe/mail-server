package org.koppe.cuf.mail.server.common.mail;

/**
 * Represents a command sent by the client
 */
public interface Command<T extends State> {
    /**
     * Gets string representation of the command
     * 
     * @return string representation of the command
     */
    public String getValue();

    /**
     * Gets the action to be executed for the command
     * 
     * @return action to be executed for the command
     */
    public CommandAction<T, ? extends Context<T, ? extends Command<T>>> getAction();
}
