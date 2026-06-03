package org.koppe.cuf.mail.server.common;

public interface Session extends Runnable {
    /**
     * Signalises whether this session is still active
     * 
     * @return True, if session is still active
     */
    public boolean isActive();

    /**
     * Closes the session and ends it
     */
    public void close();

    /**
     * Adds a server that subscribes to updates of this sessions status
     * 
     * @param server Server that should subscribe to this sessions status
     */
    public void addSubscribedServer(Server server);
}
