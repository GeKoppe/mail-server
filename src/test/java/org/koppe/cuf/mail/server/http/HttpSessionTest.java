package org.koppe.cuf.mail.server.http;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.http.dto.LoginDto;
import org.koppe.cuf.mail.server.http.endpoints.GetMailEndpoint;
import org.koppe.cuf.mail.server.http.endpoints.LoginEndpoint;
import org.koppe.cuf.mail.server.http.entities.Method;

public class HttpSessionTest {
    @Test
    void testRun() throws IOException {
        String input = "Host: localhost\r\nUser-Agent: Teststream\r\nAccept: */*\r\n\r\n";
        InputStream is = new ByteArrayInputStream(input.getBytes());
        OutputStream os = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);

        HttpSession<Void, Mail> session = HttpSession.of(socket,
                new FirstLine("/mail/1", Method.GET, "HTTP/1.1"), new GetMailEndpoint(),
                new BufferedReader(new InputStreamReader(is)), new PrintWriter(os));
        session.run();

        String result = os.toString();
        is.close();
        os.close();
        socket.close();
        assertTrue(result.contains("Server: Host"));
        assertTrue(result.contains("HTTP/1.1 200 OK"));
        assertTrue(result.contains("{"));

        InputStream is2 = new ByteArrayInputStream(input.getBytes());
        OutputStream os2 = new ByteArrayOutputStream();

        Socket socket2 = mock(Socket.class);
        HttpSession<LoginDto, Void> s2 = HttpSession.of(socket2,
                new FirstLine("/login", Method.GET, "HTTP/1.1"), new LoginEndpoint(),
                new BufferedReader(new InputStreamReader(is2)), new PrintWriter(os2));
        s2.run();

        String result2 = os2.toString();
        is2.close();
        os2.close();
        socket2.close();

        assertTrue(result2.contains("HTTP/1.1 200 OK"));
    }

    void testNotFound() {
        String input = "Host: localhost\r\nUser-Agent: Teststream\r\nAccept: */*\r\n\r\n";
        InputStream is = new ByteArrayInputStream(input.getBytes());
        OutputStream os = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);
        HttpSession<Void, Mail> session = HttpSession.of(socket,
                new FirstLine("/mail/1", Method.GET, "HTTP/1.1"), new GetMailEndpoint(),
                new BufferedReader(new InputStreamReader(is)), new PrintWriter(os));
        session.run();
    }
}
