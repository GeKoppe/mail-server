package org.koppe.cuf.mail.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.HTTPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.HTTP_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.IMAPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.IMAP_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.SMTPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.SMTP_PORT;

import javax.naming.ConfigurationException;

import org.koppe.cuf.mail.server.config.NetworkConfig;

/**
 * Initializer for all configs
 */
public class ConfigInitializer {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ConfigInitializer.class);

    /**
     * Initializes all configurations
     * 
     * @throws ConfigurationException
     */
    public static final void execute() throws ConfigurationException {
        logger.info("Initializing all server configs");
        initNetworkConfig();
    }

    /**
     * Initializes the network config for the system
     * 
     * @throws ConfigurationException
     */
    private static final void initNetworkConfig() throws ConfigurationException {
        try {
            logger.info("Initializing network config");
            logger.debug("Loading environment variables");

            Integer http = System.getenv(HTTP_PORT) != null ? Integer.parseInt(System.getenv(HTTP_PORT)) : 80;
            Integer https = System.getenv(HTTPS_PORT) != null ? Integer.parseInt(System.getenv(HTTPS_PORT)) : 443;
            Integer smtp = System.getenv(SMTP_PORT) != null ? Integer.parseInt(System.getenv(SMTP_PORT)) : 25;
            Integer smtps = System.getenv(SMTPS_PORT) != null ? Integer.parseInt(System.getenv(SMTPS_PORT)) : 587;
            Integer imap = System.getenv(IMAP_PORT) != null ? Integer.parseInt(System.getenv(IMAP_PORT)) : 143;
            Integer imaps = System.getenv(IMAPS_PORT) != null ? Integer.parseInt(System.getenv(IMAPS_PORT)) : 993;
            logger.debug(
                    "Loaded environment variables.\n\tHTTP: {}\n\tHTTPS: {}\n\tSMTP: {}\n\tSMTPS: {}\n\tIMAP: {}\n\tIMAPS: {}",
                    http, https,
                    smtp, smtps, imap, imaps);

            logger.debug("Setting config values");
            NetworkConfig.HTTP_PORT = http;
            NetworkConfig.HTTPS_PORT = https;
            NetworkConfig.SMTP_PORT = smtp;
            NetworkConfig.SMTPS_PORT = smtps;
            NetworkConfig.IMAP_PORT = imap;
            NetworkConfig.IMAPS_PORT = imaps;

            logger.debug("Set all config values for network configuration");
        } catch (NumberFormatException ex) {
            logger.info("Invalid configuration in one or more port configurations caused an exception.", ex);
            throw new ConfigurationException("Invalid configuration in one or more port configurations");
        }
    }
}
