package org.koppe.cuf.mail.server.smtp.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.common.security.TLSWrapper;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTlsAction implements CommandAction<SmtpState, SmtpContext> {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(StartTlsAction.class);

    /**
     * Wraps the socket in tls
     */
    @Override
    public void apply(SmtpContext c) {
        WritingUtils.write(c.getWriter(), "220 Ready to start TLS");
        Socket socket = TLSWrapper.wrapTls(c.getSocket());
        if (socket == null) {
            logger.info("Could not wrap socket in tls");
            c.setState(SmtpState.CONNECTION_ERROR);
            return;
        }

        logger.debug("Socket successfully wrapped in tls");
        c.setSocket(socket);

        try {
            c.setWriter(new PrintWriter(socket.getOutputStream()));
            c.setReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        } catch (IOException e) {
            logger.warn("Could not set reader or writer for the new socket");
            c.setState(SmtpState.CONNECTION_ERROR);
        }
        c.setState(SmtpState.EHLO);
    }

}
