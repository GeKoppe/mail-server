package org.koppe.cuf.mail.server.http.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.exceptions.AuthenticationException;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class JwtAuthenticatorTest {
    @Test
    void testAuthenticate() throws AuthenticationException {
        JwtAuthenticator auth = new JwtAuthenticator();
        assertNull(auth.authenticate(null));
        assertNull(auth.authenticate("Test"));

        Request<String> request = Request.empty(new TypeReference<String>() {

        });

        assertNull(auth.authenticate(request));

        Map<String, String> headers = new HashMap<>();
        request.setHeaders(headers);
        assertNull(auth.authenticate(request));

        headers.put("Authorization", "");
        assertNull(auth.authenticate(request));

        headers.put("Authorization", "test");
        assertNull(auth.authenticate(request));

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

        String token = JwtUtils.generateToken(user.getName());
        headers.put("Authorization", token);

        User u = auth.authenticate(request);
        assertNotNull(u);

        assertEquals(user.getMail(), u.getMail());
        assertEquals(user.getId(), u.getId());
        assertEquals(user.getName(), u.getName());

        fact.close();
    }
}
