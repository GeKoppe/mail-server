package org.koppe.cuf.mail.server.common.exceptions;

import lombok.NoArgsConstructor;

/**
 * Thrown when authentication fails horribly
 */
@NoArgsConstructor
public class AuthenticationException extends Exception {
    public AuthenticationException(String msg) {
        super(msg);
    }

    public AuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
