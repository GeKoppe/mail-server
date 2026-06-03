package org.koppe.cuf.mail.server.common;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public class HttpInitializerTest {
    @Test
    void testInitializeEndpoints() throws StartupException {
        HttpInitializer.announce(new Endpoint<Void, String>() {
            @Getter
            private Method method = Method.GET;
            @Getter
            private final Path path = Path.of("/mails/{id}", Map.of("id", "Integer"));
            @Getter
            private boolean asList = false;

            @Override
            public TypeReference<Void> getInputType() {
                return new TypeReference<Void>() {
                };
            }

            @Override
            public Response<String> handle(Request<Void> i) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handle'");
            }

            @Override
            public boolean isAuthenticated() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'isAuthenticated'");
            }

            @Override
            public TypeReference<String> getOutputType() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getOutputType'");
            }

        });
        assertThrows(StartupException.class, () -> HttpInitializer.initializeEndpoints());
    }
}
