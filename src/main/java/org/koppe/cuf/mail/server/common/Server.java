package org.koppe.cuf.mail.server.common;

/**
 * Implemented by all server instances
 */
public interface Server extends Runnable {
    /**
     * Shuts down the server instance
     */
    public void shutdown();

    /**
     * Notifies server of an event.
     * 
     * @param <T>   Cause / Trigger of the event
     * @param <I>   Type of information the event is conveying
     * @param event The event
     */
    public <T, I> void notify(Event<T, I> event);
}
