package org.koppe.cuf.mail.server.db.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.JpaRepository;

public class FolderServiceTest {

    private static SessionFactory fact;
    private static JpaRepository<Folder, Long> repo = new JpaRepository<>(Folder.class);
    private static JpaRepository<User, Integer> uRepo = new JpaRepository<>(User.class);

    @BeforeAll
    public static void beforeEach() {
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
            s.createQuery("FROM Folder", Folder.class).getResultList();
        }
        repo.setFactory(fact);
        uRepo.setFactory(fact);
    }

    // @Test
    void testSave() {
        FolderService fs = new FolderService();
        UserService us = new UserService();
        us.setRepo(uRepo);
        fs.setRepo(repo);

        User u = new User();
        u.setCreated(LocalDate.now());
        u.setName("test");
        u.setPw("test");

        User saved = us.save(u);

        Folder folder = new Folder();
        folder.setName("Test");
        folder.setOwner(saved);

        Folder f = fs.save(folder);
        assertNotNull(f);
        assertEquals(1, f.getId());

        assertNull(fs.save(new Folder()));
    }

    @Test
    void testFindById() {

    }

    @Test
    void testFindByName() {

    }

    @Test
    void testFindByOwner() {

    }
}
