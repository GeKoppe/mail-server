package org.koppe.cuf.mail.server.smtp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.jupiter.api.Test;

public class SmtpSessionTest {
    Socket socket = mock(Socket.class);

    @Test
    public void testSmtp() throws IOException {
        String input = "EHLO testclient\r\nMAIL FROM:<test@test.com>\r\nRCPT TO:<rcpt@test.com>\r\nDATA\r\nSubject: Testmail\r\nCc: cc@test.com\r\n\r\nTesting new mail client\r\n.";
        ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
        OutputStream bos = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(is);
        when(socket.getOutputStream()).thenReturn(bos);

        SmtpSession session = new SmtpSession(socket, "localhost");
        session.run();

        String result = bos.toString();
        assertTrue(result.contains("250 OK"));
    }
}
