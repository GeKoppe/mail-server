package org.koppe.cuf.mail.server.db.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;

public class MailRepositoryTest {

    private static SessionFactory fact;
    private static MailRepository repo;
    private static Mail mail;

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

        repo = new MailRepository();
        repo.setFactory(fact);

        mail = new Mail();
        mail.setDeleted(false);
        mail.setGuid(UUID.randomUUID().toString());
        mail.setFilePath("/0/" + mail.getGuid() + ".eml");
        mail.setFrom("test@test.com");
        mail.setMetadata(new ArrayList<>());
        mail.setRead(false);
        mail.setReceived(LocalDateTime.now());
        mail.setSubject("Test");
        mail.setUser(new ArrayList<>());
    }

    @AfterAll
    static void tearDown() {
        fact.close();
    }

    @BeforeEach
    void beforeAll() {
        mail.setGuid(UUID.randomUUID().toString());
        mail.setFilePath("/0/" + mail.getGuid() + ".eml");
    }

    @Test
    void testFindByGuid() {
        try {
            repo.save(mail);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        List<Mail> opt = repo.findByGuid(mail.getGuid());
        assertFalse(opt.isEmpty());

        Mail received = opt.get(0);
        mail.setId(received.getId());
        assertEquals(mail, received);
        mail.setId(null);
    }

    @Test
    void testFindById() {
        try {
            mail.setId(repo.save(mail).getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Optional<Mail> opt = repo.findById(mail.getId());
        assertFalse(opt.isEmpty());

        Mail received = opt.get();
        assertEquals(mail, received);
        mail.setId(null);
    }

    @Test
    void testUpdate() {
        Mail toUpdate;
        try {
            toUpdate = repo.save(mail);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        toUpdate.setRead(true);
        toUpdate.setGuid("123");

        Mail updated;
        try {
            updated = repo.update(toUpdate);
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        assertEquals(updated, toUpdate);
    }
}
