package org.koppe.cuf.mail.server.http.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.exceptions.AuthenticationException;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.UserService;

public class JwtUtilsTest {

    private static SessionFactory fact;

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

        UserRepository repo = new UserRepository();
        repo.setFactory(fact);
        UserService us = new UserService();
        us.setRepo(repo);

        JwtUtils.setSrv(us);

        User user = new User();
        user.setName("Test");
        user.setMail("test");
        user.setPw(PwHash.hash("test"));
        user.setCreated(LocalDate.now());

        us.create(user);
    }

    @AfterAll
    public static void tearDown() {
        fact.close();
    }

    @Test
    void testGenerateRefreshToken() throws AuthenticationException {
        String username = "Test";
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
    void testGenerateToken() throws AuthenticationException {
        String username = "Test";
        String jwt = JwtUtils.generateToken(username);

        assertEquals(username, JwtUtils.getUser(jwt));
    }

    @Test
    void testGetUser() throws AuthenticationException {
        String username = "Test";
        String jwt = JwtUtils.generateToken(username);

        assertEquals(username, JwtUtils.getUser(jwt));
    }

    @Test
    void testRefresh() throws AuthenticationException {
        String username = "Test";
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
    void testValidate() throws AuthenticationException {
        String jwt = JwtUtils.generateToken("Test");

        assertTrue(JwtUtils.validate(jwt));
    }
}
