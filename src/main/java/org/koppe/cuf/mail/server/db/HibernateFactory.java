package org.koppe.cuf.mail.server.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;

import lombok.Getter;

public abstract class HibernateFactory {
    /**
     * Session factory
     */
    @Getter
    private static SessionFactory sessionFactory = null;

    // #region build session factory
    /**
     * Initializes the hibernate session factory. Only initializes the factory
     * exactly once, if the method is called again, nothing will happen.
     * 
     * @throws StartupException If hibernate could not be initialized.
     */
    public static void buildSessionFactory() throws StartupException {
        if (sessionFactory != null)
            return;

        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new StartupException("Could not initialize hibernate factory", ex);
        }
    }
}
