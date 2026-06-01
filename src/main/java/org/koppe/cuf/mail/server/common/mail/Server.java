package org.koppe.cuf.mail.server.common.mail;

/**
 * Implemented by all server instances
 */
public interface Server extends Runnable {
    /**
     * Shuts down the server instance
     */
    public void shutdown();
}
