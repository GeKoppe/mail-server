package org.koppe.cuf.mail.server.common;

/**
 * Implemented by all server instances
 */
public interface Server extends Runnable {
    /**
     * Shuts down the server instance
     */
    public void shutdown();

    public <T, I> void notify(Event<T, I> event);
}
