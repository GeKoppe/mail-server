package org.koppe.cuf.mail.server.smtp.actions;

import java.io.IOException;

import org.koppe.cuf.mail.server.common.mail.CommandAction;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.state.SmtpContext;
import org.koppe.cuf.mail.server.smtp.state.SmtpState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAction implements CommandAction<SmtpState, SmtpContext> {
    private final Logger logger = LoggerFactory.getLogger(DataAction.class);

    @Override
    public void apply(SmtpContext c) {
        WritingUtils.write(c.getWriter(), "354 Start mail input, end with a single do");
        logger.debug("Start reading mail contents");
        String line;
        boolean headersDone = false;
        StringBuilder body = new StringBuilder();

        try {
            while ((line = c.getReader().readLine()) != null) {
                // If the line is a single dot, message has ended
                if (line.equals(".")) {
                    logger.info("Finished reading mail from client");
                    c.getMail().setBody(body.toString());
                    WritingUtils.write(c.getWriter(), "250 OK");
                    c.setState(SmtpState.DONE);
                    return;
                }

                if (line.equals("")) {
                    logger.debug("Found empty line, start reading body");
                    headersDone = true;
                    continue;
                }

                if (!headersDone) {
                    String[] header = line.split(": ", 2);
                    c.getMail().getHeader().put(header[0], header[1].trim());

                    if (header[0].equals("Cc"))
                        c.getMail().getCc().add(header[1]);

                    if (header[0].equals("Bcc"))
                        c.getMail().getBcc().add(header[1]);

                    if (header[0].equals("Subject"))
                        c.getMail().setSubject(header[1]);

                    continue;
                }

                if (line.startsWith(".."))
                    line = line.substring(1);

                body.append(line).append("\r\n");
            }

            logger.warn("Client closed connection unexpectedly");
            c.setState(SmtpState.CLIENT_ERROR);
        } catch (IOException e) {
            logger.info("Connection exception", e);
            c.setState(SmtpState.CONNECTION_ERROR);
        }
    }

}
