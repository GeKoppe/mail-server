package org.koppe.cuf.mail.server.http.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TokenException extends Exception {
    public TokenException(String message) {
        super(message);
    }

    public TokenException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
