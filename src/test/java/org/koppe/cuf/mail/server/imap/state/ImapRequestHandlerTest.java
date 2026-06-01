package org.koppe.cuf.mail.server.imap.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.mail.Request;

public class ImapRequestHandlerTest {
    @Test
    void testRead() {
        String input = "a1 NOOP test test test test";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes())));
        ImapRequestHandler handler = new ImapRequestHandler();
        Request r = handler.read(reader);

        assertEquals(ImapCommand.NOOP, r.command());
        assertEquals("test test test test", r.arguments().get("args"));
        assertEquals("a1", r.arguments().get("tag"));

        input = "a1 UID FETCH test test test test";
        reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes())));
        handler = new ImapRequestHandler();
        r = handler.read(reader);

        assertEquals(ImapCommand.FETCH_UID, r.command());
        assertEquals("test test test test", r.arguments().get("args"));
        assertEquals("a1", r.arguments().get("tag"));

        input = "UID FETCH test test test test";
        reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getBytes())));
        handler = new ImapRequestHandler();
        r = handler.read(reader);

        assertEquals(ImapCommand.ERROR, r.command());
    }
}
