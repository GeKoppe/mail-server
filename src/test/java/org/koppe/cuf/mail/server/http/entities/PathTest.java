package org.koppe.cuf.mail.server.http.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.http.endpoints.GetMailEndpoint;

public class PathTest {

    @Test
    void testOf() {
        assertThrows(IllegalArgumentException.class, () -> Path.of("/mail/{id", null));
        assertThrows(IllegalArgumentException.class, () -> Path.of("/mail/{id/}", null));
        assertThrows(IllegalArgumentException.class, () -> Path.of("/mail/{id}", null));
        assertThrows(IllegalArgumentException.class, () -> Path.of("/mail/{{id}", null));
        assertThrows(IllegalArgumentException.class, () -> Path.of("/mail/{id}/test}", null));
    }

    @Test
    void testMatches() {
        assertTrue(Path.of("/mail/{id}", Map.of("id", "Integer")).matches("/mail/1"));
        assertFalse(Path.of("/mail/{id}", Map.of("id", "Integer")).matches("/mail/a"));
        assertTrue(Path.of("/mail/{id}/from/{sender}/test", Map.of("id", "Integer", "sender", "String"))
                .matches("/mail/1/from/test/test"));
    }

    @Test
    void testGetArguments() {
        Map<String, Object> args = Path.getArguments("/mail/1/from/test/test",
                Path.of("/mail/{id}/from/{sender}/test", Map.of("id", "Integer", "sender", "String")));

        assertNotNull(args);
        assertNotNull(args.get("id"));
        assertNotNull(args.get("sender"));

        Object sender = args.get("sender");
        Object id = args.get("id");

        if (!(sender instanceof String)) {
            fail();
        }

        if (!(id instanceof Integer)) {
            fail();
        }

        assertEquals("test", (String) sender);
        assertEquals(1, (Integer) id);
    }

    @Test
    void testEquals() {
        Path p1 = Path.of("/mails/{id}", Map.of("id", "Integer"));
        Path p2 = Path.of("/mails/{identification}", Map.of("identification", "Integer"));
        GetMailEndpoint ep = new GetMailEndpoint();

        assertTrue(p1.equals(p2));
        assertTrue(ep.getPath().equals(p2));
    }

    @Test
    void testHashcode() {
        Path p1 = Path.of("/mail/{id}/test", Map.of("id", "Integer"));
        Path p2 = Path.of("/mail/{identification}/test", Map.of("identification", "Integer"));

        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
