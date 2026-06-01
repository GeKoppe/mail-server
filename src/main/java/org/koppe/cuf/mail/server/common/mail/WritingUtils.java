package org.koppe.cuf.mail.server.common.mail;

import java.io.IOException;
import java.io.Writer;

public class WritingUtils {
    public static boolean write(Writer w, String msg) {
        try {
            w.write(msg + "\r\n");
            w.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
