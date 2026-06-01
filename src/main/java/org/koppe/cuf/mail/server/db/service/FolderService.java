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

@RequiredArgsConstructor
public class FolderService {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(FolderService.class);
    /**
     * Jpa repository
     */
    private final JpaRepository<Folder, Long> repo = new JpaRepository<>(Folder.class);

    // #region find by id
    public @Nullable Folder findById(long id) {
        return repo.findById(id).orElse(null);
    }

    public @NotNull List<Folder> findByName(String name) {
        if (name == null || name.isBlank()) {
            logger.info("No name for folder to find given");
            return null;
        }

        return repo.findBy(Folder_.name, name);
    }

    public @NotNull List<Folder> findByOwner(int ownerId) {
        return repo.findBy(Folder_.owner, "" + ownerId);
    }
}
