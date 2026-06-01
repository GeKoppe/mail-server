package org.koppe.cuf.mail.server.smtp.state;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.koppe.cuf.mail.server.common.mail.Request;
import org.koppe.cuf.mail.server.common.mail.RequestHandler;

import lombok.RequiredArgsConstructor;

/**
 * Handler class for smtp requests. Parses the client input to a valid smtp
 * command.
 */
@RequiredArgsConstructor
public class SmtpRequestHandler implements RequestHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public Request read(BufferedReader reader) {
        String line = null;

        try {
            line = reader.readLine();
        } catch (IOException e) {
            return null;
        }

        String command = line.split(" ", 2)[0].toUpperCase();
        String argument = line.contains(" ") ? line.substring(line.indexOf(' ') + 1) : "";

        SmtpCommand comm = SmtpCommand.ofValue(command);
        Map<String, String> args = new HashMap<>();
        args.put("args", argument);

        return new Request(comm, args);
    }

}
