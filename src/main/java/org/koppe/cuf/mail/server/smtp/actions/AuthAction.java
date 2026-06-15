package org.koppe.cuf.mail.server.smtp.actions;

import java.util.List;

import org.koppe.cuf.mail.server.common.PwHash;
import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.jpa.User_;
import org.koppe.cuf.mail.server.db.repository.JpaRepository;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action when client wants to authorise itself
 */
public class AuthAction implements CommandAction<SmtpState, SmtpContext> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(AuthAction.class);
    /**
     * Jpa repository for checking the database
     */
    private final JpaRepository<User, Integer> users = new JpaRepository<>(User.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(SmtpContext c) {
        if (c.getUser() != null) {
            logger.warn("User already authenticated");
            c.setState(SmtpState.CLIENT_ERROR);
            return;
        }
        logger.debug("Authenticating session");
        if (c.getArguments().get("args") == null || c.getArguments().get("args").isBlank()) {
            logger.error("No credentials sent in request");
            c.setState(SmtpState.CLIENT_ERROR);
            return;
        }

        String[] args = c.getArguments().get("args").split(" ");
        if (args.length < 2) {
            logger.warn("Client did not send username or password");
            WritingUtils.write(c.getWriter(), "535 FAILURE user or password missing");
            c.setState(SmtpState.CLIENT_ERROR);
            return;
        }

        String user = args[0];
        String pw = args[1];

        List<User> found = users.findBy(User_.name, user);
        if (found.size() < 1 || !PwHash.matches(pw, found.get(0).getPw())) {
            logger.error("Invalid credentials given");
            WritingUtils.write(c.getWriter(), "535 FAILURE user or password missing");
            c.setState(SmtpState.CLIENT_ERROR);
            return;
        }

        logger.debug("Session authenticated");
        WritingUtils.write(c.getWriter(), "235 OK");

        c.setUser(found.get(0));
        return;
    }

}
