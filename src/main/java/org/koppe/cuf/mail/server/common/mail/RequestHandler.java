package org.koppe.cuf.mail.server.common.mail;

import java.io.BufferedReader;

@FunctionalInterface
public interface RequestHandler {
    /**
     * Reads the request from the reader
     * 
     * @param reader
     * @return
     */
    public Request read(BufferedReader reader);
}
