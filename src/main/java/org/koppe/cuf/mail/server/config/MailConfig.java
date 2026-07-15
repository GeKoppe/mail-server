package org.koppe.cuf.mail.server.config;

import java.util.Set;

public class MailConfig {
    /**
     * Maximum mail size in kilobytes
     */
    public static long MAX_MAIL_SIZE;
    /**
     * Domains this server belongs to
     */
    public static Set<String> DOMAINS;
    public static String MAIL_STORE_DIRECTORY = "/opt/cuf/mails/store";
}
