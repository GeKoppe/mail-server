package org.koppe.cuf.mail.server.http.endpoints;

import java.util.Map;

import org.koppe.cuf.mail.server.http.dto.RefreshDto;
import org.koppe.cuf.mail.server.http.dto.SessionDto;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;
import org.koppe.cuf.mail.server.http.utils.TokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public class RefreshEndpoint implements Endpoint<RefreshDto, SessionDto> {
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
    private final Path path = Path.of("/login/refresh", Map.of());
    /**
     * Type of input
     */
    @Getter
    private final TypeReference<RefreshDto> inputType = new TypeReference<RefreshDto>() {
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
     * {@inheritDoc}
     */
    @Override
    public Response<SessionDto> handle(Request<RefreshDto> i) {
        if (i.getBody() == null || i.getBody().getObject() == null || i.getBody().getObject().getRefresh() == null
                || i.getBody().getObject().getRefresh().isBlank()) {
            logger.debug("Invalid request body, cannot create refreshed auth token");
            return Response.unauthorized();
        }

        String refresh = i.getBody().getObject().getRefresh();
        String newJwt;
        try {
            newJwt = JwtUtils.refresh(refresh);
        } catch (TokenException e) {
            logger.debug("Refresh token is invalid", e);
            return Response.unauthorized();
        }

        return Response.ok(new SessionDto(newJwt, refresh), new TypeReference<SessionDto>() {
        }, MediaType.APPLICATION_JSON);
    }
}
