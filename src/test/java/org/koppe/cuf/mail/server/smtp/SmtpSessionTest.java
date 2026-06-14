package org.koppe.cuf.mail.server.smtp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.koppe.cuf.mail.server.common.ConfigInitializer;
import org.koppe.cuf.mail.server.common.exceptions.ConfigurationException;
import org.koppe.cuf.mail.server.config.NetworkConfig;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class SmtpSessionTest {
    @SystemStub
    private EnvironmentVariables env = new EnvironmentVariables()
            .set("DOMAINS", "test.com");

    @Test
    public void testSmtp() throws InterruptedException {
        try {
            ConfigInitializer.execute();
        } catch (ConfigurationException e) {
            fail(e.getMessage());
        }

        SmtpServer server = new SmtpServer("localhost", NetworkConfig.SMTP_PORT);
        Thread t = Thread.ofVirtual().factory().newThread(server);
        t.start();
        Thread.sleep(200);

        Socket socket;
        try {
            StringBuilder sb = new StringBuilder();
            socket = new Socket("localhost", NetworkConfig.SMTP_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            sb.append(in.readLine()); // 220 greeting

            out.println("EHLO test");
            while (true) {
                String line = in.readLine();
                sb.append(line);
                if (!line.startsWith("250-"))
                    break;
            }

            out.println("STARTTLS");
            sb.append(in.readLine()); // 220 Ready to start TLS

            // TLS Upgrade
            TrustManager[] trustAll = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new SecureRandom());
            SSLSocketFactory sslFactory = (SSLSocketFactory) sc.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(
                    socket, "localhost", NetworkConfig.SMTP_PORT, true);
            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();

            // Ab jetzt über den SSLSocket kommunizieren!
            in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            out = new PrintWriter(sslSocket.getOutputStream(), true);

            // Nochmal EHLO nach TLS
            out.println("EHLO test");
            while (true) {
                String line = in.readLine();
                sb.append(line);
                if (!line.startsWith("250-"))
                    break;
            }

            out.println("MAIL FROM:<test@test.de>");
            sb.append(in.readLine()); // 250 OK

            out.println("RCPT TO:<recipient@test.de>");
            sb.append(in.readLine()); // 250 OK

            out.println("DATA");
            sb.append(in.readLine()); // 354 Start input

            out.println("Subject: Test");
            out.println("From: test@test.de");
            out.println("To: empfaenger@test.de");
            out.println(""); // leere Zeile = Header/Body Trenner
            out.println("Hello World!");
            out.println("."); // Ende der Mail
            sb.append(in.readLine()); // 250 OK

            assertTrue(sb.toString().contains("250 OK"));
            assertTrue(sb.toString().contains("SMTPUTF8"));
            assertTrue(sb.toString().contains("220"));
            socket.close();
            in.close();
            out.close();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.shutdown();
        }
    }
}
