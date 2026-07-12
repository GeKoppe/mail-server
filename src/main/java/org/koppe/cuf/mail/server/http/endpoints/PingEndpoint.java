package org.koppe.cuf.mail.server.http.endpoints;

import java.util.HashMap;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public class PingEndpoint implements Endpoint<Void, Void> {
    /**
     * Path of the endpoint
     */
    @Getter
    private final Path path = Path.of("/ping", new HashMap<>());
    /**
     * Method of the endpoint
     */
    @Getter
    private Method method = Method.GET;
    /**
     * Type of the body allowed on this endpoint
     */
    @Getter
    private final TypeReference<Void> inputType = new TypeReference<Void>() {
    };
    /**
     * Type of response body
     */
    @Getter
    private final TypeReference<Void> outputType = new TypeReference<Void>() {
    };
    /**
     * This endpoint needs authentication
     */
    @Getter
    private boolean authenticated = true;
    @Getter
    private boolean asList;

    @Override
    public Response<Void> handle(Request<Void> i) {
        return Response.of(200, "Ok", ResponseBody.of(null, null, outputType, MediaType.APPLICATION_JSON),
                outputType, new HashMap<>());
    }
}
