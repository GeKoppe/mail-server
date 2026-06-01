package org.koppe.cuf.mail.server.common.mail;

import javax.naming.OperationNotSupportedException;

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

    public Mail getMail() throws OperationNotSupportedException;
}
