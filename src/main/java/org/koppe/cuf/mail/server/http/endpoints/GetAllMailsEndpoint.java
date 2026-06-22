package org.koppe.cuf.mail.server.http.endpoints;

import java.util.HashMap;
import java.util.List;

import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public class GetAllMailsEndpoint implements Endpoint<Void, List<Mail>> {
    /**
     * Path of the endpoint
     */
    @Getter
    private final Path path = Path.of("/mails", new HashMap<>());
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
    private final TypeReference<List<Mail>> outputType = new TypeReference<List<Mail>>() {
    };
    /**
     * This endpoint needs authentication
     */
    @Getter
    private boolean authenticated = true;

    @Override
    public Response<List<Mail>> handle(Request<Void> i) {
        Mail m1 = new Mail();
        m1.setFrom("1@2.com");
        m1.setSubject("Hello World");
        m1.setBody("Body");

        Mail m2 = new Mail();
        m2.setFrom("2@3.com");
        m2.setSubject("Goodbye world");
        m2.setBody("Boooooody");
        return Response.of(200, "Ok", ResponseBody.of(List.of(m1, m2), outputType, MediaType.APPLICATION_JSON),
                outputType, new HashMap<>());
    }
}
