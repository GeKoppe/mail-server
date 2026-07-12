package org.koppe.cuf.mail.server.http.entities;

import org.koppe.cuf.mail.server.common.Authenticator;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates jwt based auth schemes.
 */
public class JwtAuthenticator implements Authenticator {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

    /**
     * {@inheritDoc}
     * 
     * Requires "auth" to be of type
     * {@link org.koppe.cuf.mail.server.http.entities.Request}.
     */
    @Override
    public User authenticate(Object auth) {
        if (auth instanceof Request<?> r) {
            return authenticateReqeust(r);
        }

        logger.info("Given object is not of type Request<?>, cannot authorise user");
        return null;
    }

    /**
     * Authenticates the request of
     * 
     * @param r
     * @return
     */
    private User authenticateReqeust(Request<?> r) {
        logger.debug("Authenticating new request");
        if (r.getHeaders() == null || r.getHeaders().isEmpty()) {
            logger.info("No headers present in http request");
            return null;
        }

        String jwt = r.getHeaders().get("Authorization");
        if (jwt == null || jwt.isBlank()) {
            logger.info("No Authorization header sent by client");
            return null;
        }

        if (!JwtUtils.validate(jwt)) {
            logger.info("Jwt is not valid");
            return null;
        }

        return JwtUtils.getJpaUser(jwt);
    }

}
