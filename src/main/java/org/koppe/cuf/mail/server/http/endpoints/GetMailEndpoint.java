package org.koppe.cuf.mail.server.http.endpoints;

import java.util.HashMap;
import java.util.Map;

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
import lombok.ToString;

@Getter
@ToString
public class GetMailEndpoint implements Endpoint<Void, Mail> {
    /**
     * Path of the endpoint
     */
    private final Path path = Path.of("/mails/{id}", Map.of("id", "Integer"));
    /**
     * Method of the endpoint
     */
    private Method method = Method.GET;
    /**
     * Type of the body allowed on this endpoint
     */
    private final TypeReference<Void> inputType = new TypeReference<Void>() {
    };
    /**
     * Type of response body
     */
    private final TypeReference<Mail> outputType = new TypeReference<Mail>() {
    };
    /**
     * This endpoint needs authentication
     */
    private boolean authenticated = true;

    /**
     * Handles the
     */
    @Override
    public Response<Mail> handle(Request<Void> i) {
        if (i.getQuery().containsKey("only_body") && i.getQuery().get("only_body").equals("true")) {
            return Response.of(200, "OK",
                    ResponseBody.of("{\"body\":\"Test\"}", null, outputType, MediaType.APPLICATION_JSON), outputType,
                    new HashMap<>());
        }
        Mail mail = new Mail();
        mail.setBody("Test");
        mail.setFrom("test@test.com");
        mail.setSubject("Test");
        mail.getCc().add("cc@test.com");
        mail.getHeader().put("Test", "Test");
        return Response.of(200, "OK",
                ResponseBody.of(mail, outputType, MediaType.APPLICATION_JSON), outputType, new HashMap<>());
    }

}
