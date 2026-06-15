package org.koppe.cuf.mail.server.common;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for encrypting and decrypting passwords using bcrypt
 */
public abstract class PwHash {
    /**
     * Pepper for the hashes
     */
    private static final String pepper = "JK!jdio23jklDfadKlO$fjdasiopj23kl?öjdIO_i3uio2jkldfas";

    /**
     * Creates a hash from the given plain password. Salt is generated randomly
     * 
     * @param pw Password to hash
     * @return Hashed password
     */
    public static String hash(String pw) {
        return BCrypt.hashpw(pw + pepper, BCrypt.gensalt(12));
    }

    /**
     * Checks if given password matches a hashed value.
     * 
     * @param pw     Plain password to be checked
     * @param hashed Hashed value password should be checked against
     * @return True, if password and hash match, false otherwise
     */
    public static boolean matches(String pw, String hashed) {
        return BCrypt.checkpw(pw + pepper, hashed);
    }
}
