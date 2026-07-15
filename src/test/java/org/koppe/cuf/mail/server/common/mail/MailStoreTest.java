package org.koppe.cuf.mail.server.common.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.config.MailConfig;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.MailRepository;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.MailService;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.java.expansion.validation.ValidationUtils;

public class MailStoreTest {
    private static SessionFactory fact;
    private static UserRepository userRepo;
    private static MailService mailService;

    @BeforeAll
    public static void setup() {
        Configuration cfg = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Folder.class)
                .addAnnotatedClass(Mail.class)
                .addAnnotatedClass(MailMetadata.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url",
                        "jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create")
                .setProperty("hibernate.show_sql", "true");

        fact = cfg.buildSessionFactory();
        try (Session s = fact.openSession()) {
            s.createQuery("FROM User", User.class).getResultList();
            s.createQuery("FROM Folder", Folder.class).getResultList();
            s.createQuery("FROM Mail", Mail.class).getResultList();
            s.createQuery("FROM MailMetadata", MailMetadata.class).getResultList();
        }

        userRepo = new UserRepository();
        userRepo.setFactory(fact);
        UserService us = new UserService();
        us.setRepo(userRepo);

        User user = new User();
        user.setName("Test");
        user.setMail("test");
        user.setPw(PwHash.hash("test"));
        user.setCreated(LocalDate.now());

        us.create(user);

        MailRepository mr = new MailRepository();
        mr.setFactory(fact);
        mailService = new MailService();
        mailService.setRepo(mr);
        MailConfig.MAIL_STORE_DIRECTORY = System.getProperty("java.io.tmpdir");
    }

    @AfterAll
    public static void tearDown() {
        fact.close();
    }

    @Test
    void testSave() {
        assertThrows(IllegalAccessError.class, () -> new MailStore(null));
        assertThrows(IllegalAccessError.class, () -> new MailStore(new User()));

        User user = userRepo.findById(1L).orElse(null);
        if (!ValidationUtils.checkNotNullOrEmpty(user)) {
            fail("Could not retrieve user from database");
        }

        try (MailStore ms = new MailStore(user)) {
            ms.setSrv(mailService);
            org.koppe.cuf.mail.server.common.mail.Mail mail = new org.koppe.cuf.mail.server.common.mail.Mail();
            assertNull(ms.save(mail));

            mail.setFrom("test@test.com");
            mail.setTo(List.of("hey@yo.com"));
            mail.setBody("Test");
            mail.setSubject("Hello World");
            mail.setBcc(List.of("hey@yo.com"));
            mail.setCc(List.of("hey@yo.com"));
            mail.setHeader(Map.of("Referrer", "None"));

            File res = ms.save(mail);
            assertNotNull(res);
            assertTrue(res.exists());

            res.deleteOnExit();

            Mail created = mailService.findById(1L);
            assertNotNull(created);

            assertEquals(1L, (long) created.getId());
            assertEquals(false, created.getDeleted());
            assertEquals(false, created.getRead());
            assertEquals("/0/" + created.getGuid() + ".eml", created.getFilePath());
        } catch (IllegalAccessError | Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetMailFile() {
        User user = userRepo.findById(1L).orElse(null);
        if (!ValidationUtils.checkNotNullOrEmpty(user)) {
            fail("Could not retrieve user from database");
        }

        try (MailStore ms = new MailStore(user)) {
            ms.setSrv(mailService);
            org.koppe.cuf.mail.server.common.mail.Mail mail = new org.koppe.cuf.mail.server.common.mail.Mail();
            mail.setFrom("test@test.com");
            mail.setTo(List.of("hey@yo.com"));
            mail.setBody("Test");
            mail.setSubject("Hello World");
            mail.setBcc(List.of("hey@yo.com"));
            mail.setCc(List.of("hey@yo.com"));
            mail.setHeader(Map.of("Referrer", "None"));

            File res = ms.save(mail);
            assertNotNull(res);
            assertTrue(res.exists());

            res.deleteOnExit();

            Mail m = mailService.findById(2L);
            assertNotNull(m);

            assertNull(ms.getMailFile(3L));

            File byId = ms.getMailFile(m.getId());
            assertEquals(res, byId);

            File byGuid = ms.getMailFile(m.getGuid());
            assertEquals(res, byGuid);
        } catch (IllegalAccessError | Exception e) {
            fail(e.getMessage());
        }
    }
}
