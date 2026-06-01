package org.koppe.cuf.mail.server.common.mail;

import java.io.BufferedReader;

@FunctionalInterface
public interface RequestHandler {
    public Request read(BufferedReader reader);
}
