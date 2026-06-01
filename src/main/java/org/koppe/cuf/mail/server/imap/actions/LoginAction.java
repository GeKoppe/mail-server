package org.koppe.cuf.mail.server.imap.actions;

import java.util.List;

import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.koppe.cuf.mail.server.imap.state.ImapContext;
import org.koppe.cuf.mail.server.imap.state.ImapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that is executed during login attempt
 */
public class LoginAction implements CommandAction<ImapState, ImapContext> {
    private final Logger logger = LoggerFactory.getLogger(LoginAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(ImapContext c) {
        logger.debug("Logging in user");
        String[] arguments = c.getArguments().get("args").split(" ");
        User user = getByMail(arguments[0], c.getUserService());
        if (user == null)
            user = getByName(arguments[0], c.getUserService());

        if (user == null) {
            logger.info("No user with given name found");
            WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " NO LOGIN failed");
            c.incClientErrors();
            return;
        }

        logger.debug("Found requested user, checking password hash");
        if (!PwHash.matches(arguments[1], user.getPw())) {
            WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " NO LOGIN failed");
            c.incClientErrors();
            return;
        }

        WritingUtils.write(c.getWriter(), c.getArguments().get("tag") + " OK LOGIN completed");
        c.setState(ImapState.AUTHENTICATED);
        return;
    }

    /**
     * Get user by mail
     * 
     * @param mail Mail of the user
     * @param srv  DB Service
     * @return The found user or null, if no such user exists
     */
    private User getByMail(String mail, UserService srv) {
        List<User> ul = srv.findByMail(mail);
        return ul.isEmpty() ? null : ul.get(0);
    }

    /**
     * Get user by name
     * 
     * @param mail Name of the user
     * @param srv  DB Service
     * @return The found user or null, if no such user exists
     */
    private User getByName(String name, UserService srv) {
        List<User> ul = srv.findByName(name);
        return ul.isEmpty() ? null : ul.get(0);
    }
}
