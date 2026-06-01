package org.koppe.cuf.mail.server.db.service;

import java.util.List;

import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.Mail_;
import org.koppe.cuf.mail.server.db.repository.JpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MailService {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(MailService.class);
    /**
     * Repository to interact with the database
     */
    private final JpaRepository<Mail, Long> repo = new JpaRepository<>(Mail.class);

    // #region find by id
    /**
     * Returns the mail with the given id or null, if no such mail exists
     * 
     * @param id Id of the mail to find
     * @return The found mail or null
     */
    public @Nullable Mail findById(long id) {
        logger.debug("Querying database for mails with id {}", id);
        return repo.findById(id).orElse(null);
    }

    // #region find by subject
    /**
     * Returns a list of mails with the given subject
     * 
     * @param subject Subject of the mails to be found
     * @return The found mails or an empty list, if no such mail exists
     */
    public @Nullable List<Mail> findBySubject(@NotNull String subject) {
        if (subject == null || subject.isBlank()) {
            logger.info("No subject given");
            return null;
        }
        logger.debug("Querying for mails with subject {}", subject);
        return repo.findBy(Mail_.subject, subject);
    }

    // #region find by sender
    /**
     * Returns a list of all mails by given sender
     * 
     * @param sender Sender of the mails to be returned
     * @return List of all found mails
     */
    public @Nullable List<Mail> findBySender(@NotNull String sender) {
        if (sender == null || sender.isBlank()) {
            logger.info("No sender given");
            return null;
        }
        logger.debug("Querying for mails with sender {}", sender);
        return repo.findBy(Mail_.from, sender);
    }

    // #region delete by id
    /**
     * Deletes mail with given id
     * 
     * @param id Id of the mail to delete
     */
    public void deleteById(long id) {
        repo.deleteById(id);
    }

    // #region save
    /**
     * Saves the given mail to the database
     * 
     * @param mail Mail to save
     * @return The saved mail
     * @throws Exception If saving to the database failed
     */
    public @NotNull Mail save(@NotNull Mail mail) throws Exception {
        return repo.save(mail);
    }
}
