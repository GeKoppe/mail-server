package org.koppe.cuf.mail.server.config;

/**
 * Network configuration
 */
public abstract class NetworkConfig {
    /**
     * HTTP port
     */
    public static int HTTP_PORT;
    /**
     * HTTPS port
     */
    public static int HTTPS_PORT;
    /**
     * SMTP port
     */
    public static int SMTP_PORT;
    /**
     * SMTPS port
     */
    public static int SMTPS_PORT;
    /**
     * IMAP port
     */
    public static int IMAP_PORT;
    /**
     * IMAPS port
     */
    public static int IMAPS_PORT;
}
