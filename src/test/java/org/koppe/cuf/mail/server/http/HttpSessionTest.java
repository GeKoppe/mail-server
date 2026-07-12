package org.koppe.cuf.mail.server.http;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.exceptions.AuthenticationException;
import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.cuf.mail.server.http.endpoints.GetMailEndpoint;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;

public class HttpSessionTest {
    private static SessionFactory fact;
    private static User user = new User();

    @BeforeAll
    public static void setup() {
        Configuration cfg = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Folder.class)
                .addAnnotatedClass(org.koppe.cuf.mail.server.db.jpa.Mail.class)
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

        user.setName("Test");
        user.setMail("test");
        user.setPw(PwHash.hash("test"));
        user.setCreated(LocalDate.now());

        us.create(user);
    }

    @AfterAll
    public static void shutdown() {
        fact.close();
    }

    @Test
    void testRun() throws IOException, AuthenticationException {
        String input = "Host: localhost\r\nUser-Agent: Teststream\r\nAccept: */*\r\nAuthorization: "
                + JwtUtils.generateToken(user.getName()) + "\r\n\r\n";
        InputStream is = new ByteArrayInputStream(input.getBytes());
        OutputStream os = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);

        HttpSession<Void, Mail> session = HttpSession.of(socket,
                new FirstLine("/mail/1", Method.GET, "HTTP/1.1"), new GetMailEndpoint(),
                new BufferedReader(new InputStreamReader(is)), new PrintWriter(os));
        session.run();

        String result = os.toString();
        is.close();
        os.close();
        socket.close();
        assertTrue(result.contains("Server: Host"));
        assertTrue(result.contains("HTTP/1.1 200 OK"));
        assertTrue(result.contains("{"));

        fact.close();
    }

    @Test
    void testNotFound() {
        String input = "Host: localhost\r\nUser-Agent: Teststream\r\nAccept: */*\r\n\r\n";
        InputStream is = new ByteArrayInputStream(input.getBytes());
        OutputStream os = new ByteArrayOutputStream();

        Socket socket = mock(Socket.class);
        HttpSession<Void, Mail> session = HttpSession.of(socket,
                new FirstLine("/mail/2", Method.GET, "HTTP/1.1"), new GetMailEndpoint(),
                new BufferedReader(new InputStreamReader(is)), new PrintWriter(os));
        session.run();
    }
}
