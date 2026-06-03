package org.koppe.cuf.mail.server.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.HttpInitializer;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpServerTest {
    private static HttpServer server = new HttpServer(6666);
    private static Thread thread;

    @BeforeAll
    public static void setup() {
        Endpoint<Void, Mail> ep = new Endpoint<Void, Mail>() {
            @Getter
            private final Method method = Method.GET;
            @Getter
            private final Path path = Path.of("/test-path", new HashMap<>());

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

            @Override
            public boolean isAsList() {
                return false;
            }
        };

        HttpInitializer.announce(ep);
        try {
            HttpInitializer.initializeEndpoints();
        } catch (StartupException e) {
            fail();
        }
        thread = Thread.ofVirtual().factory().newThread(server);
        thread.start();
    }

    @AfterAll
    public static void shutdown() throws InterruptedException {
        server.shutdown();
        thread.join();
    }

    @Test
    void testRun() {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(60, TimeUnit.SECONDS)
                .build();

        Request r = new Request.Builder()
                .url("http://localhost:6666/test-path")
                .header("Content-Length", "0")
                .get()
                .build();

        try (Response res = client.newCall(r).execute()) {
            assertEquals(200, res.code());
            String body = res.body().string();
            Mail m = new ObjectMapper().readValue(body, Mail.class);
            assertEquals("Test", m.getSubject());
            assertEquals("test@test.com", m.getFrom());
        } catch (IOException e) {
            fail();
        }

        Request r2 = new Request.Builder()
                .url("http://localhost:6666/test-path?only_body=true")
                .header("Content-Length", "0")
                .get()
                .build();

        try (Response res2 = client.newCall(r2).execute()) {
            assertEquals(200, res2.code());
            String body = res2.body().string();
            Mail m = new ObjectMapper().readValue(body, Mail.class);
            assertNull(m.getSubject());
            assertEquals("Testbody", m.getBody());
        } catch (IOException e) {
            fail();
        }
    }
}
