package org.koppe.cuf.mail.server.common.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

public class WritingUtilsTest {
    @Test
    void testWrite() {
        try (OutputStream os = new ByteArrayOutputStream();
                PrintWriter w = new PrintWriter(os)) {
            WritingUtils.write(w, "Hello World");
            assertEquals("Hello World\r\n", os.toString());
        } catch (IOException e) {
            fail();
        }
    }
}
