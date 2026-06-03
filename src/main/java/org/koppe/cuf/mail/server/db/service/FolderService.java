package org.koppe.cuf.mail.server.db.service;

import java.util.List;

import org.koppe.cuf.mail.server.db.jpa.Folder;
import org.koppe.cuf.mail.server.db.jpa.Folder_;
import org.koppe.cuf.mail.server.db.repository.JpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class FolderService {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(FolderService.class);
    /**
     * Jpa repository
     */
    @Setter
    private JpaRepository<Folder, Long> repo = new JpaRepository<>(Folder.class);

    // #region find by id
    /**
     * Finds a folder by it's id
     * 
     * @param id Id of the folder to find
     * @return Found folder
     */
    public @Nullable Folder findById(long id) {
        return repo.findById(id).orElse(null);
    }

    /**
     * Finds all folders matching the given name
     * 
     * @param name Name of the folders to find
     * @return Found folders
     */
    public @NotNull List<Folder> findByName(String name) {
        if (name == null || name.isBlank()) {
            logger.info("No name for folder to find given");
            return null;
        }

        return repo.findBy(Folder_.name, name);
    }

    /**
     * TODO implement correctly
     * Finds all folders belonging to given owner
     * 
     * @param ownerId Id of the owner
     * @return All folders belonging to the owner
     */
    public @NotNull List<Folder> findByOwner(int ownerId) {
        return repo.findBy(Folder_.owner, "" + ownerId);
    }

    public @Nullable Folder save(Folder folder) {
        try {
            return repo.save(folder);
        } catch (Exception e) {
            logger.warn("Could not save folder due to exception", e);
            return null;
        }
    }
}
