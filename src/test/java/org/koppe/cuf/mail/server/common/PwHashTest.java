package org.koppe.cuf.mail.server.common;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PwHashTest {
    @Test
    void testHash() {
        String res = PwHash.hash("test123");
        assertNotEquals(res, "test123");
    }

    @Test
    void testMatches() {
        assertTrue(PwHash.matches("test123", PwHash.hash("test123")));
    }
}
