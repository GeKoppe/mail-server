package org.koppe.cuf.mail.server.http.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.UserService;

public class JwtUtilsTest {
    @Test
    void testGenerateRefreshToken() {
        String username = "test";
        String refresh = JwtUtils.generateRefreshToken(username);

        String jwt;
        try {
            jwt = JwtUtils.refresh(refresh);
        } catch (TokenException e) {
            fail(e.getMessage());
            return;
        }
        assertEquals(username, JwtUtils.getUser(jwt));
    }

    @Test
    void testGenerateToken() {
        String username = "test";
        String jwt = JwtUtils.generateToken(username);

        assertEquals(username, JwtUtils.getUser(jwt));
    }

    @Test
    void testGetUser() {
        String username = "test";
        String jwt = JwtUtils.generateToken(username);

        assertEquals(username, JwtUtils.getUser(jwt));
    }

    @Test
    void testRefresh() {
        String username = "test";
        String refresh = JwtUtils.generateRefreshToken(username);

        String jwt;
        try {
            jwt = JwtUtils.refresh(refresh);
        } catch (TokenException e) {
            fail(e.getMessage());
            return;
        }
        assertEquals(username, JwtUtils.getUser(jwt));
    }

    // @Test
    void testValidate() {
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

        SessionFactory fact = cfg.buildSessionFactory();
        try (Session s = fact.openSession()) {
            s.createQuery("FROM User", User.class).getResultList();
        }
        UserRepository repo = new UserRepository();
        repo.setFactory(fact);
        UserService us = new UserService();
        us.setRepo(repo);

        User user = new User();
        user.setId(1);
        user.setName("Test");

        us.save(user);
        String jwt = JwtUtils.generateToken(user.getName());

        assertTrue(JwtUtils.validate(jwt, us));
    }
}
