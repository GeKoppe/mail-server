package org.koppe.cuf.mail.server.common.exceptions;

import lombok.NoArgsConstructor;

/**
 * Signalises an exception on starting the application
 */
@NoArgsConstructor
public class StartupException extends Exception {
    /**
     * Constructor that sets message and cause of the exception, as well as
     * initialising it.
     * 
     * @param message Message of the exception
     * @param cause   Cause of the exception
     */
    public StartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
