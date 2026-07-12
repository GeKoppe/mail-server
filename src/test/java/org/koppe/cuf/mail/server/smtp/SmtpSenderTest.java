package org.koppe.cuf.mail.server.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.smtp.entities.DNSCache;

import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.internet.MimeMessage;

public class SmtpSenderTest {

    @Test
    void testSend() throws InterruptedException {
        try {
            GreenMail greenMail = new GreenMail(new ServerSetup[] { new ServerSetup(3000, "localhost", "smtp"),
                    new ServerSetup(3001, "localhost", "smtps") });
            try {
                greenMail.getUserManager().createUser("test@test.com", "test", "123");
            } catch (UserException e) {
                fail(e.getMessage());
            }
            greenMail.start();

            Mail mail = new Mail();
            mail.setFrom("test@test.com");
            mail.getTo().add("test@pm.me");
            mail.getTo().add("test@gmail.com");
            mail.getTo().add("test@outlook.com");
            mail.getCc().add("test@test.com");
            mail.getBcc().add("test@gmail.com");
            mail.setSubject("TEST");
            mail.setBody("Hello World.\r\n.This is a test");
            mail.getHeader().put("Test", "Test");

            new SmtpSender(false).send(mail);

            Thread.sleep(2000);
            MimeMessage[] msg = greenMail.getReceivedMessages();
            greenMail.stop();
            assertEquals(1, msg.length);
            assertTrue(DNSCache.get("pm.me") != null);
        } catch (Throwable ex) {

        }
    }
}
