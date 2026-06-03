package org.koppe.cuf.mail.server.db.service;

import java.util.List;

import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.jpa.User_;
import org.koppe.cuf.mail.server.db.repository.JpaRepository;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

public class UserService {
    /**
     * Repository for interacting with the database
     */
    @Setter
    private JpaRepository<User, Integer> repo = new JpaRepository<>(User.class);

    // #region find by id
    /**
     * Finds user by id
     * 
     * @param id Id of the user to find
     * @return The user or null, if no such user could be found
     */
    public @Nullable User findById(int id) {
        return repo.findById(id).orElse(null);
    }

    public @NotNull List<User> findByName(@NotNull String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return repo.findBy(User_.name, name);
    }

    public @NotNull List<User> findByMail(@NotNull String mail) {
        if (mail == null || mail.isBlank())
            return null;
        return repo.findBy(User_.mail, mail);
    }

    // #region find all by name
    /**
     * Finds all users with given name
     * 
     * @param name Name of the user to find
     * @return List of all matching users
     */
    public @NotNull List<User> findAllByName(@NotNull String name) {
        return repo.findBy(User_.name, name);
    }

    // #region delete by id
    /**
     * Deletes user by id
     * 
     * @param id Id of the user to be deleted
     */
    public void deleteById(int id) {
        repo.deleteById(id);
    }

    public User save(User u) {
        try {
            return repo.save(u);
        } catch (Exception e) {
            return null;
        }
    }
}
