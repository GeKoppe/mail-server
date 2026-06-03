package org.koppe.cuf.mail.server.http.endpoints;

import java.util.Map;

import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import lombok.Getter;
import lombok.ToString;

@ToString
public class GetMailEndpoint implements Endpoint<Void, Mail> {
    /**
     * Path of the endpoint
     */
    @Getter
    private final Path path = Path.of("/mails/{id}", Map.of("id", "Integer"));
    /**
     * Method of the endpoint
     */
    @Getter
    private Method method = Method.GET;
    /**
     * Type of the body allowed on this endpoint
     */
    @Getter
    private Class<Void> inputType = Void.class;
    /**
     * Type of response body
     */
    @Getter
    private Class<Mail> outputType = Mail.class;
    /**
     * This endpoint needs authentication
     */
    @Getter
    private boolean authenticated = true;
    @Getter
    private boolean asList;

    /**
     * Handles the
     */
    @Override
    public Response<Mail> handle(Request<Void> i) {
        if (i.getQuery().containsKey("only_body") && i.getQuery().get("only_body").equals("true")) {
            return Response.of(200, "OK",
                    ResponseBody.of("{\"body\":\"Test\"}", null, Mail.class, MediaType.APPLICATION_JSON), Mail.class);
        }
        Mail mail = new Mail();
        mail.setBody("Test");
        mail.setFrom("test@test.com");
        mail.setSubject("Test");
        mail.getCc().add("cc@test.com");
        mail.getHeader().put("Test", "Test");
        return Response.of(200, "OK",
                ResponseBody.of(mail, Mail.class, MediaType.APPLICATION_JSON), Mail.class);
    }

}
