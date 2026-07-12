package org.koppe.cuf.mail.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.koppe.cuf.mail.server.config.EnvironmentConfig.ALLOW_PLAIN;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.DOMAINS;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.HTTPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.HTTP_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.IMAPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.IMAP_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.KEYSTORE_PASS;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.KEYSTORE_PATH;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.MAX_MAIL_SIZE;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.SMTPS_PORT;
import static org.koppe.cuf.mail.server.config.EnvironmentConfig.SMTP_PORT;

import java.util.Set;

import org.koppe.cuf.mail.server.common.exceptions.ConfigurationException;
import org.koppe.cuf.mail.server.config.MailConfig;
import org.koppe.cuf.mail.server.config.NetworkConfig;
import org.koppe.cuf.mail.server.config.SecurityConfig;

/**
 * Initializer for all configs
 */
public class ConfigInitializer {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ConfigInitializer.class);

    // #region execute
    /**
     * Initializes all configurations
     * 
     * @throws ConfigurationException If the given configuration parameters are
     *                                invalid
     */
    public static final void execute() throws ConfigurationException {
        logger.info("Initializing all server configs");
        initNetworkConfig();
        initSecurityConfig();
        initMailConfig();
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

            Integer http = System.getenv(HTTP_PORT) != null ? Integer.parseInt(System.getenv(HTTP_PORT)) : 6600;
            Integer https = System.getenv(HTTPS_PORT) != null ? Integer.parseInt(System.getenv(HTTPS_PORT)) : 6603;
            Integer smtp = System.getenv(SMTP_PORT) != null ? Integer.parseInt(System.getenv(SMTP_PORT)) : 6500;
            Integer smtps = System.getenv(SMTPS_PORT) != null ? Integer.parseInt(System.getenv(SMTPS_PORT)) : 6503;
            Integer imap = System.getenv(IMAP_PORT) != null ? Integer.parseInt(System.getenv(IMAP_PORT)) : 6400;
            Integer imaps = System.getenv(IMAPS_PORT) != null ? Integer.parseInt(System.getenv(IMAPS_PORT)) : 6403;
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
            logger.error("Invalid configuration in one or more port configurations caused an exception.", ex);
            throw new ConfigurationException("Invalid configuration in one or more port configurations");
        }
    }

    // #region init security config
    /**
     * Initializes the security configuration for the server
     * 
     * @throws ConfigurationException
     */
    private static final void initSecurityConfig() throws ConfigurationException {
        logger.info("Loading security config from environment");
        try {
            SecurityConfig.KEYSTORE_PATH = System.getenv(KEYSTORE_PATH) != null ? System.getenv(KEYSTORE_PATH)
                    : ConfigInitializer.class.getClassLoader().getResource("security/devstore.jks").getFile();
            SecurityConfig.KEYSTORE_PASS = System.getenv(KEYSTORE_PASS) != null ? System.getenv(KEYSTORE_PASS)
                    : "changeit";
            SecurityConfig.ALLOW_PLAIN = System.getenv(ALLOW_PLAIN) != null
                    && System.getenv(ALLOW_PLAIN).equals("True");

            if (!SecurityConfig.ALLOW_PLAIN
                    && (SecurityConfig.KEYSTORE_PATH == null || SecurityConfig.KEYSTORE_PATH.isBlank()
                            || SecurityConfig.KEYSTORE_PASS == null || SecurityConfig.KEYSTORE_PASS.isBlank())) {
                logger.error(
                        "Plain communication not allowed but keystore path and keystore pass are not defined in environment");
                throw new ConfigurationException(
                        "Plain communication not allowed but keystore path and keystore pass are not defined in environment");
            }
            logger.info("Initialised security config");
        } catch (Exception ex) {
            logger.error("Invalid configuration in on or more env variables", ex);
            throw new ConfigurationException(
                    "Plain communication not allowed but keystore path and keystore pass are not defined in environment",
                    ex);
        }
    }

    // #region init mail config
    /**
     * Initializes mail configuration
     * 
     * @throws ConfigurationException If no domains for this system are configured
     *                                or an invalid MAX_MAIL_SIZE is given
     */
    private static final void initMailConfig() throws ConfigurationException {
        logger.info("Initialising mail config");
        try {
            MailConfig.MAX_MAIL_SIZE = System.getenv(MAX_MAIL_SIZE) != null
                    ? Long.parseLong(System.getenv(MAX_MAIL_SIZE))
                    : 32768L;

            if (System.getenv(DOMAINS) == null) {
                logger.error("No domains configured for this server");
                throw new ConfigurationException("Missing domain configuration");
            }
            MailConfig.DOMAINS = Set.of(System.getenv(DOMAINS).split(","));
        } catch (Exception ex) {
            logger.error("Exception occurred while trying to initialize mail config", ex);
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }
}
