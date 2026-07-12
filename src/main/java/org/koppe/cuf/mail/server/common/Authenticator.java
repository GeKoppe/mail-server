package org.koppe.cuf.mail.server.common;

import org.koppe.cuf.mail.server.db.jpa.User;

import jakarta.annotation.Nullable;

/**
 * Used for authenticating users at the system
 */
@FunctionalInterface
public interface Authenticator {
    /**
     * Authenticates the given object. Implementations of this interface might use
     * different types of credentials.
     * 
     * @param auth Credential object specific for the implementation
     * @return The authenticated user or null, if user could not be authenticated
     */
    public @Nullable User authenticate(Object auth);
}
