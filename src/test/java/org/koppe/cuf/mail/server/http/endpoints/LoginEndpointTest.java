package org.koppe.cuf.mail.server.http.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.repository.UserRepository;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.cuf.mail.server.http.dto.LoginDto;
import org.koppe.cuf.mail.server.http.dto.SessionDto;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.RequestBody;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.utils.JwtUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class LoginEndpointTest {

    private static SessionFactory fact;
    private static UserRepository userRepo;
    private static UserService srv;

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
        srv = new UserService();
        srv.setRepo(userRepo);

        User user = new User();
        user.setName("Test");
        user.setMail("test");
        user.setPw(PwHash.hash("test"));
        user.setCreated(LocalDate.now());

        srv.create(user);
        JwtUtils.setSrv(srv);
    }

    @AfterAll
    static void tearDown() {
        fact.close();
    }

    @Test
    void testHandle() {
        LoginEndpoint ep = new LoginEndpoint();
        ep.setRepo(srv);

        assertEquals(401, ep.handle(null).getCode());
        Request<LoginDto> r = Request.empty(new TypeReference<LoginDto>() {

        });
        assertEquals(401, ep.handle(r).getCode());

        r.setBody(RequestBody.empty(new TypeReference<LoginDto>() {
        }));
        assertEquals(401, ep.handle(r).getCode());

        LoginDto dto = new LoginDto();
        r.setBody(RequestBody.of(dto, new TypeReference<LoginDto>() {
        }));
        assertEquals(401, ep.handle(r).getCode());

        dto.setPassword(" ");
        dto.setUser(" ");
        assertEquals(401, ep.handle(r).getCode());

        dto.setPassword("Wrong password");
        dto.setUser("Wrong Username");
        assertEquals(401, ep.handle(r).getCode());

        dto.setUser("Test");
        assertEquals(401, ep.handle(r).getCode());

        dto.setPassword("test");
        Response<SessionDto> resp = ep.handle(r);

        assertNotNull(resp);
        assertEquals(200, resp.getCode());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getObject());
        assertNotNull(resp.getBody().getObject().getJwt());

        assertTrue(JwtUtils.validate(resp.getBody().getObject().getJwt()));
    }
}
