package org.koppe.cuf.mail.server.config;

public class EnvironmentConfig {
    // #region network
    /**
     * HTTP port
     */
    public static final String HTTP_PORT = "HTTP_PORT";
    /**
     * HTTPS port
     */
    public static final String HTTPS_PORT = "HTTPS_PORT";
    /**
     * SMTP port
     */
    public static final String SMTP_PORT = "SMTP_PORT";
    /**
     * SMTPS port
     */
    public static final String SMTPS_PORT = "SMTPS_PORT";
    /**
     * IMAP port
     */
    public static final String IMAP_PORT = "IMAP_PORT";
    /**
     * IMAPS port
     */
    public static final String IMAPS_PORT = "IMAPS_PORT";

    // #region security
    /**
     * Keystore path
     */
    public static final String KEYSTORE_PATH = "KEYSTORE_PATH";
    /**
     * Keystore password
     */
    public static final String KEYSTORE_PASS = "KEYSTORE_PASS";
    public static final String ALLOW_PLAIN = "ALLOW_PLAIN";

    // #region mail
    public static final String MAX_MAIL_SIZE = "MAX_MAIL_SIZE";
    public static final String DOMAINS = "DOMAINS";
}
