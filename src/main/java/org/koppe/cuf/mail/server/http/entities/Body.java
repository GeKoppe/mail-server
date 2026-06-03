package org.koppe.cuf.mail.server.http.entities;

import java.io.IOException;

/**
 * Body of an http message
 */
public interface Body {
    /**
     * String value of the stream
     * 
     * @return String value of the stream
     * @throws IOException See the implementation
     */
    public String getString() throws IOException;
}
