package org.koppe.cuf.mail.server.smtp.state;

import java.io.BufferedReader;
import java.io.Writer;
import java.util.Map;

import org.koppe.cuf.mail.server.common.mail.Context;
import org.koppe.cuf.mail.server.common.mail.Mail;

import lombok.Data;

/**
 * Class for holding all current context information for the SMTP protocol
 */
@Data
public class SmtpContext implements Context<SmtpState, SmtpCommand> {
    private volatile Mail mail;
    private volatile String hostname;
    private volatile SmtpState state;
    private BufferedReader reader;
    private Writer writer;
    private Map<String, String> arguments;
    private volatile boolean active;
    private volatile SmtpCommand clientCommand;

    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
    }
}
