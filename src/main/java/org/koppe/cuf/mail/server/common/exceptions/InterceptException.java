package org.koppe.cuf.mail.server.common.exceptions;

import lombok.NoArgsConstructor;

/**
 * Thrown when an {@link org.koppe.cuf.mail.server.common.Interceptor} had an
 * exception during execution
 */
@NoArgsConstructor
public class InterceptException extends Exception {
    /**
     * All args constructor
     * 
     * @param msg   Message of the exception
     * @param cause Cause for the exception
     */
    public InterceptException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
