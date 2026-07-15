package org.koppe.cuf.mail.server.common.mail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.koppe.cuf.mail.server.config.MailConfig;
import org.koppe.cuf.mail.server.db.jpa.MailMetadata;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.service.MailService;
import org.koppe.java.expansion.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides interaction with database and file system for mail files.
 */
public class MailStore implements AutoCloseable {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(MailStore.class);
    /**
     * Folder the mails are stored in
     */
    @Getter
    private static final File mailFolder = new File(MailConfig.MAIL_STORE_DIRECTORY);
    /**
     * Service to interact with the database
     */
    @Setter
    private MailService srv = new MailService();
    /**
     * User to open this mail store
     */
    private final User user;

    /**
     * Default constructor. Creates the directory for storing tha mails and sets the
     * user that's opening the mail store.
     * 
     * @param user User to open the mail store
     * @throws IOException        If directory that holds the mails could not be
     *                            initialized
     * @throws IllegalAccessError If an invalid user is given
     */
    public MailStore(User user) throws IOException, IllegalAccessError {
        if (!ValidationUtils.checkNotNullOrEmpty(user)
                || !ValidationUtils.checkNotNullOrEmpty(user.getId(), user.getName())) {
            logger.info("Invalid user given, cannot save mail for that user");
            throw new IllegalAccessError("Invalid user");
        }
        if (!mailFolder.exists()) {
            Files.createDirectories(mailFolder.toPath());
        }
        this.user = user;
    }

    /**
     * Saves the mail to the filesystem and to the database
     * 
     * @param mail Mail to save
     * @return The saved mail file
     * @throws Exception If saving the mail failed somehow.
     */
    public File save(Mail mail) throws Exception {
        if (!validate(mail)) {
            logger.debug("Invalid mail given");
            return null;
        }
        logger.debug("Saving mail");

        String uuid = UUID.randomUUID().toString();
        org.koppe.cuf.mail.server.db.jpa.Mail m = saveToDb(mail, uuid);

        File mailFile = createFile(mail, uuid, m.getId());
        m.setFilePath(mailFile.getAbsolutePath().substring(mailFolder.getAbsolutePath().length()));
        m = srv.update(m);

        logger.debug("Mail successfully saved");
        return mailFile;
    }

    /**
     * Validates that all necessary elements of the mail (mail itself, from, to,
     * headers) are present
     * 
     * @param mail Mail to valdiate
     * @return True, if no necessary parts are null or missing
     */
    private final boolean validate(Mail mail) {
        return ValidationUtils.checkNotNullOrEmpty(mail)
                && ValidationUtils.checkNotNullOrEmpty(mail.getFrom(), mail.getTo(), mail.getHeader());
    }

    /**
     * Creates the mail file
     * 
     * @param mail   Mail the file is based on
     * @param uuid   UUID of the mail in the database
     * @param mailId Id of the mail in the database
     * @return The ccreated file
     * @throws IOException If file or path to the file could not be created
     */
    private final File createFile(Mail mail, String uuid, long mailId) throws IOException {
        long dir = Math.floorDiv(mailId, 10000);
        File directory = createNewDirectory(dir);

        logger.debug("Creating mail file");
        File m = new File(Path.of(directory.toPath().toString(), uuid + ".eml").toString());
        m.createNewFile();

        logger.debug("Writing stream to mail file");
        try (FileWriter fw = new FileWriter(m)) {
            fw.write(mail.toFileString());
        }

        return m;
    }

    /**
     * Creates new directory with the given number
     * 
     * @param dir Directory number to create or return, if exists
     * @return The new directory or the existing one, if one with that id already
     *         exists
     * @throws IOException If directory could not be created
     */
    private synchronized static File createNewDirectory(long dir) throws IOException {
        Path path = Path.of(mailFolder.getAbsolutePath(), "" + dir);
        File directory = new File(path.toString());
        if (!directory.exists()) {
            Files.createDirectories(path);
        }

        return directory;
    }

    /**
     * Saves the mail to the database
     * 
     * @param mail Mail to save
     * @param uuid Generated uuid for the mail
     * @return The saved mail
     * @throws Exception If mail could not be saved
     */
    private final org.koppe.cuf.mail.server.db.jpa.Mail saveToDb(Mail mail, String uuid) throws Exception {
        logger.debug("Save mail {} to database", uuid);
        org.koppe.cuf.mail.server.db.jpa.Mail m = new org.koppe.cuf.mail.server.db.jpa.Mail();
        m.setDeleted(false);
        m.setFrom(mail.getFrom());
        m.setMetadata(new ArrayList<>());

        logger.debug("Set metadata");
        mail.getHeader().entrySet().forEach(x -> {
            MailMetadata meta = new MailMetadata();
            meta.setMail(m);
            meta.setKey(x.getKey());
            meta.setValue(x.getValue());
            m.getMetadata().add(meta);
        });

        m.setRead(false);
        m.setReceived(LocalDateTime.now());
        m.setSubject(mail.getSubject());
        List<User> users = new ArrayList<>();
        users.add(user);
        m.setUser(users);
        m.setGuid(uuid);
        return srv.save(m);
    }

    /**
     * Returns file associated with given mail id
     * 
     * @param id Id of the mail
     * @return The found file or null, if no such file exists
     */
    public File getMailFile(long id) {
        logger.debug("Retrieving mail with id {}", "" + id);
        return getMailFile(srv.findById(id));
    }

    public File getMailFile(String guid) {
        logger.debug("Retrieving mail with guid {}", guid);
        return getMailFile(srv.findByGuid(guid).orElse(null));
    }

    private File getMailFile(org.koppe.cuf.mail.server.db.jpa.Mail mail) {
        if (!ValidationUtils.checkNotNullOrEmpty(mail) || !ValidationUtils.checkNotNullOrEmpty(mail.getFilePath())) {
            logger.debug("Invalid mail given");
            return null;
        }

        String filePath = mail.getFilePath();
        File file = new File(Path.of(mailFolder.getAbsolutePath(), filePath).toString());
        if (!file.exists()) {
            logger.warn("File with path {} does not exist, database inconsistent", file.getAbsolutePath());
            return null;
        }
        logger.debug("Found mail file");

        return file;
    }

    @Override
    public void close() throws Exception {

    }

}
