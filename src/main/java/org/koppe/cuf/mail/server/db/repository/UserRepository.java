package org.koppe.cuf.mail.server.db.repository;

import java.util.List;

import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.jpa.User_;

/**
 * Repository for interacting with users in the database
 */
public class UserRepository extends JpaRepository<User, Long> {

    /**
     * Default constructor
     */
    public UserRepository() {
        super(User.class);
    }

    // #region find by name
    /**
     * Looks for all users with given name
     * 
     * @param name Name of the user to find
     * @return List of all users with the given name
     */
    public User findByName(String name) {
        List<User> found = super.findBy(User_.name, name);
        return found.isEmpty() ? null : found.getFirst();
    }

    // #region find by mail
    /**
     * Returns all users with given mail
     * 
     * @param mail Mail of the user to find
     * @return All users with given mail
     */
    public User findByMail(String mail) {
        List<User> found = super.findBy(User_.mail, mail);
        return found.isEmpty() ? null : found.getFirst();
    }

    /**
     * Checks if user with given username exists
     * 
     * @param username Username to check for
     * @return
     */
    public boolean existsByName(String username) {
        return super.existsBy(User_.name, username);
    }

}
