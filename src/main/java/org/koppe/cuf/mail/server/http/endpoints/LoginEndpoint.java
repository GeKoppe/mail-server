package org.koppe.cuf.mail.server.http.endpoints;

import java.util.List;
import java.util.Map;

import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.exceptions.AuthenticationException;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.cuf.mail.server.http.dto.LoginDto;
import org.koppe.cuf.mail.server.http.dto.SessionDto;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;
import org.koppe.java.expansion.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;
import lombok.Setter;

/**
 * Endpoint for logging into the system
 */
public class LoginEndpoint implements Endpoint<LoginDto, SessionDto> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(LoginEndpoint.class);
    /**
     * Method
     */
    @Getter
    private final Method method = Method.POST;
    /**
     * Path
     */
    @Getter
    private final Path path = Path.of("/login", Map.of());
    /**
     * Type of input
     */
    @Getter
    private final TypeReference<LoginDto> inputType = new TypeReference<LoginDto>() {
    };
    /**
     * Output type
     */
    @Getter
    private final TypeReference<SessionDto> outputType = new TypeReference<SessionDto>() {
    };
    /**
     * Does not need authentication
     */
    @Getter
    private final boolean authenticated = false;
    /**
     * Repository for interacting with users in the database
     */
    @Setter
    private UserService repo = new UserService();

    /**
     * Handles the actual request
     */
    @Override
    public Response<SessionDto> handle(Request<LoginDto> i) {
        if (i == null || i.getBody() == null || i.getBody().getObject() == null) {
            logger.info("Invalid request, no credentials given");
            return Response.unauthorized();
        }

        LoginDto body = i.getBody().getObject();
        if (!ValidationUtils.checkNotNullOrEmpty(body.getPassword(), body.getUser())) {
            logger.info("No credentials given");
            return Response.unauthorized();
        }
        logger.debug("Authenticating {}", body.getUser());

        User user = getDbUser(body.getUser());
        if (user == null) {
            logger.info("Invalid credentials given");
            return Response.unauthorized();
        }

        if (!PwHash.matches(body.getPassword(), user.getPw())) {
            logger.info("Invalid credentials given");
            return Response.unauthorized();
        }

        SessionDto dto = null;
        try {
            dto = new SessionDto(JwtUtils.generateToken(user.getName()),
                    JwtUtils.generateRefreshToken(user.getName()));
        } catch (AuthenticationException e) {
            logger.warn("Invalid username");
            return Response.unauthorized();
        }

        logger.debug("Authorised user {}", user.getName());

        return Response.ok(dto, getOutputType(), MediaType.APPLICATION_JSON);
    }

    /**
     * Queries the database for user with given username
     * 
     * @param username Name of the user to find
     * @return The found user or null, if no such user exists
     */
    private User getDbUser(String username) {
        logger.debug("Getting db user for username {}", username);
        List<User> users = repo.findByName(username);
        return users.isEmpty() ? null : users.getFirst();
    }
}
