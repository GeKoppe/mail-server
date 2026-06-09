package org.koppe.cuf.mail.server.common.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigurationException extends Exception {
    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
