package org.koppe.cuf.mail.server.common.mail;

import java.util.Map;

/**
 * Request sent by a client
 */
public record Request(Command<? extends State> command, Map<String, String> arguments) {
}
