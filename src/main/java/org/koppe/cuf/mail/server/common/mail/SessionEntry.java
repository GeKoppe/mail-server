package org.koppe.cuf.mail.server.common.mail;

/**
 * Symbolises a session and it's own thread
 */
public record SessionEntry(Thread thread, Session session) {
}
