package org.koppe.cuf.mail.server.http.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;

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
}
