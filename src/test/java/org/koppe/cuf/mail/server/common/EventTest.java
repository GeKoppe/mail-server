package org.koppe.cuf.mail.server.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.Socket;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.smtp.SmtpSession;

public class EventTest {
    @Test
    void testOf() {
        SmtpSession s = new SmtpSession(new Socket(), "");
        Event<SmtpSession, String> e = Event.of(s, "Hello World");

        assertEquals(s, e.getCause());
        assertEquals("Hello World", e.getInformation());
    }
}
