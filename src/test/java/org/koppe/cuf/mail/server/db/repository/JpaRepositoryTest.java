package org.koppe.cuf.mail.server.db.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JpaRepositoryTest {
    private static SessionFactory fact;

    private static JpaRepository<TestEntity, Long> repo = new JpaRepository<>(TestEntity.class);

    @BeforeEach
    public void setup() {
        Configuration cfg = new Configuration()
                .addAnnotatedClass(TestEntity.class)
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
            s.createQuery("FROM TestEntity", TestEntity.class).getResultList();
        }
        repo.setFactory(fact);
    }

    @AfterEach
    public void shutdown() {
        if (fact != null)
            fact.close();
    }

    @Test
    void testSave() {
        TestEntity e = new TestEntity();
        e.setTestValue("Hello World");

        try {
            repo.save(e);
        } catch (Exception e1) {
            Assertions.fail();
        }

        Assertions.assertNotNull(repo.findById(e.getId()));
    }

    @Test
    void testFindBy() {
        TestEntity ent = new TestEntity();
        ent.setTestValue("Hello World");
        try {
            repo.save(ent);
        } catch (Exception e) {
            fail();
        }

        List<TestEntity> e = repo.findBy(TestEntity_.testValue, "Hello World");
        assertNotNull(e);
        assertFalse(e.isEmpty());

        assertEquals(1, e.getFirst().getId());
    }

    @Test
    void testFindById() {
        TestEntity ent = new TestEntity();
        ent.setTestValue("Hello World");
        try {
            repo.save(ent);
        } catch (Exception e) {
            fail();
        }
        Optional<TestEntity> e = repo.findById(1L);

        assertTrue(e.isPresent());
        TestEntity entity = e.get();

        assertEquals("Hello World", entity.getTestValue());

        e = repo.findById(2L);
        assertTrue(e.isEmpty());
    }

    @Test
    void testDeleteById() {
        TestEntity ent = new TestEntity();
        ent.setTestValue("Hello World");
        try {
            repo.save(ent);
        } catch (Exception e) {
            fail();
        }
        repo.deleteById(1L);
        assertTrue(repo.findById(1L).isEmpty());
    }
}
