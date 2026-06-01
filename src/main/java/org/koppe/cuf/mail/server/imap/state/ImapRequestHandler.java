package org.koppe.cuf.mail.server.imap.state;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.koppe.cuf.mail.server.common.mail.Request;
import org.koppe.cuf.mail.server.common.mail.RequestHandler;

public class ImapRequestHandler implements RequestHandler {
    private final String IMAP_PATTERN = "^[A-Za-z0-9*]+\\s+(LOGIN|SELECT|FETCH|STORE|LOGOUT|UID FETCH|UID SEARCH|UID STORE|LIST|STATUS|APPEND|DELETE|EXAMINE|CREATE|RENAME|SEARCH|CAPABILITY|NOOP)(?:\\s+.+)?$";

    @Override
    public Request read(BufferedReader reader) {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            return new Request(ImapCommand.ERROR, new HashMap<>());
        }

        line = line.trim();
        if (!line.matches(IMAP_PATTERN)) {
            return new Request(ImapCommand.ERROR, null);
        }

        Map<String, String> args = new HashMap<>();

        String[] parts = List.of(line.split(" ")).stream().filter(s -> s != null && !s.isBlank())
                .toArray(String[]::new);

        if (parts.length < 2) {
            return new Request(ImapCommand.ERROR, null);
        }
        if (!ImapCommand.ofValue(parts[0]).equals(ImapCommand.ERROR)
                || !ImapCommand.ofValue(parts[0] + " " + parts[1]).equals(ImapCommand.ERROR)) {
            return new Request(ImapCommand.ERROR, null);
        }

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("UID")) {
                if (parts.length <= i + 1 || !parts[i + 1].matches("(SEARCH|FETCH|STORE)")) {
                    return new Request(ImapCommand.ERROR, null);
                }
            }
        }

        String tag = parts[0];
        String command = parts[1];
        String arguments = parts.length > 2 ? parts[2] : "";

        if (command.equals("UID") && parts.length > 2) {
            command = "UID " + parts[2];
            arguments = "";
        }

        if (parts.length > 3) {
            for (int i = 3; i < parts.length; i++) {
                arguments += " " + parts[i];
            }
        }

        arguments = arguments.trim();
        args.put("tag", tag);
        args.put("args", arguments);

        return new Request(ImapCommand.ofValue(command), args);
    }

}
