package org.koppe.cuf.mail.server.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.koppe.cuf.mail.server.db.HibernateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class JpaRepository<T, K> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(JpaRepository.class);
    /**
     * Type of the repository
     */
    private final Class<T> type;
    /**
     * Session factory
     */
    @Setter
    private SessionFactory factory = HibernateFactory.getSessionFactory();

    // #region execute
    /**
     * Execution of reading actions
     * 
     * @param <X> Return type of reading action
     * @param f   Action to be executed
     * @return Return of the action
     */
    private <X> Optional<X> execute(Function<Session, X> f) {
        try (Session session = factory.openSession()) {
            return Optional.ofNullable(f.apply(session));
        }
    }

    // #region execute in tx
    /**
     * Execution of writing actions.
     * 
     * @param cs Action to be executed
     */
    private void executeInTx(Consumer<Session> cs) {
        Transaction tx = null;
        try (Session session = factory.openSession()) {
            tx = session.beginTransaction();
            cs.accept(session);
            tx.commit();
        } catch (Exception ex) {
            if (tx != null)
                tx.rollback();

            throw ex;
        }
    }

    // #region find by id
    /**
     * Returns an optional of the object associated with the given id.
     * 
     * @param id Id of the given object
     * @return An optional of the object
     */
    public Optional<T> findById(K id) {
        return execute((s) -> {
            return s.get(type, id);
        });
    }

    // #region
    /**
     * Saves object to the database.
     * 
     * @param t Object to be saved
     * @return The saved object or null, if an exception occurred
     * @throws Exception if saving to the database failed
     */
    public @NotNull T save(@NotNull T t) throws Exception {
        executeInTx((s) -> {
            s.persist(t);
        });
        return t;
    }

    public @Nullable T update(@NotNull T t, @NotNull K id, @NotNull BiConsumer<T, T> updater) throws Exception {
        executeInTx(s -> {
            T current = s.get(type, id);
            updater.accept(current, t);
            T updated = s.merge(current);
            updater.accept(t, updated);
        });
        return t;
    }

    // #region find by
    /**
     * Generic find by method. Returns all entities with property matching the given
     * value.
     * 
     * @param property Property to filter the resultset by
     * @param value    Value the property should match
     * @return List of all entities matching the criteria.
     */
    public @NotNull <V> List<T> findBy(SingularAttribute<T, V> property, @NotNull String value) {
        return findBy(property.getName(), value, "=");
    }

    public <V> boolean existsBy(SingularAttribute<T, V> property, @NotNull String value) {
        return !findBy(property, value).isEmpty();
    }

    // #region find like
    /**
     * Generic find by method. Returns all entities with property matching the given
     * value.
     * 
     * @param property Property to filter the resultset by
     * @param value    Vablue the property should match
     * @return List of all entities matching the criteria.
     */
    public @NotNull <V> List<T> findLike(@NotNull SingularAttribute<T, V> property, @NotNull String value) {
        return findBy(property.getName(), value, "LIKE");
    }

    // #region find by
    /**
     * Generic find by method. Returns all entities with property matching the given
     * value.
     * 
     * @param property   Property to filter the resultset by
     * @param value      Value the property should match
     * @param comparator The comparison operator, i.e. "=" or "LIKE".
     * @return List of all entities matching the criteria.
     */
    private @NotNull List<T> findBy(@NotNull String property, @NotNull String value, @NotNull String comparator) {
        return execute((s) -> {
            return s
                    .createQuery("FROM " + type.getSimpleName() + " WHERE " + property + " " + comparator + " :"
                            + property, type)
                    .setParameter(property, value)
                    .getResultList();
        }).orElseGet(List::of);
    }

    // #region delete by id
    /**
     * Deletes the entity with the given id
     * 
     * @param id Id of the entity to be deleted
     */
    public void deleteById(K id) {
        executeInTx((s) -> {
            T t = s.get(type, id);
            if (t == null) {
                logger.debug("No entity with given id found");
                return;
            }
            s.remove(t);
        });
    }
}
