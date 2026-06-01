package org.koppe.cuf.mail.server.imap.actions;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.db.service.FolderService;
import org.koppe.cuf.mail.server.db.service.MailService;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectAction implements CommandAction<ImapState, ImapContext> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(SelectAction.class);
    /**
     * Service for getting mails from the database
     */
    private final MailService mails = new MailService();
    /**
     * Service for getting folders from the database
     */
    private final FolderService folders = new FolderService();

    @Override
    public void apply(ImapContext c) {
        logger.debug("SELECT action called by client");
    }

}
