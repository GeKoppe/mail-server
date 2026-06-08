package org.koppe.cuf.mail.server.common;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.http.endpoints.GetMailEndpoint;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public class HttpInitializerTest {
    @Test
    void testInitializeException() {
        Endpoint<Void, Mail> ep = new Endpoint<Void, Mail>() {
            @Getter
            private final Method method = Method.GET;
            @Getter
            private final Path path = Path.of("/mails/{id}", Map.of("id", "Integer"));

            @Override
            public TypeReference<Void> getInputType() {
                return new TypeReference<Void>() {

                };
            }

            @Override
            public TypeReference<Mail> getOutputType() {
                return new TypeReference<Mail>() {

                };
            }

            @Override
            public org.koppe.cuf.mail.server.http.entities.Response<Mail> handle(
                    org.koppe.cuf.mail.server.http.entities.Request<Void> i) {
                if (i.getQuery().get("only_body") != null && i.getQuery().get("only_body").equals("true")) {
                    return org.koppe.cuf.mail.server.http.entities.Response.of(200, null,
                            ResponseBody.of("{\"body\": \"Testbody\"}", null, getOutputType(),
                                    MediaType.APPLICATION_JSON),
                            getOutputType());
                }
                Mail mail = new Mail();
                mail.setFrom("test@test.com");
                mail.setBody("Testbody");
                mail.setSubject("Test");
                return org.koppe.cuf.mail.server.http.entities.Response.of(200, null,
                        ResponseBody.of(mail, getOutputType(), MediaType.APPLICATION_JSON), getOutputType());
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }
        };

        HttpInitializer.announce(ep);
        HttpInitializer.announce(new GetMailEndpoint());

        assertThrows(StartupException.class, () -> HttpInitializer.initializeEndpoints());
    };
}
