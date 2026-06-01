package org.koppe.cuf.mail.server.common;

import org.mindrot.jbcrypt.BCrypt;

public abstract class PwHash {
    public static String hash(String pw) {
        return BCrypt.hashpw(pw, BCrypt.gensalt(12));
    }

    public static boolean matches(String pw, String hashed) {
        return BCrypt.checkpw(pw, hashed);
    }
}
