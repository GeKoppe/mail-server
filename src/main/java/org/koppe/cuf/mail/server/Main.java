package org.koppe.cuf.mail.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.koppe.cuf.mail.server.common.ConfigInitializer;
import org.koppe.cuf.mail.server.common.HttpInitializer;
import org.koppe.cuf.mail.server.common.Server;
import org.koppe.cuf.mail.server.common.exceptions.ConfigurationException;
import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.config.NetworkConfig;
import org.koppe.cuf.mail.server.config.SecurityConfig;
import org.koppe.cuf.mail.server.http.HttpServer;
import org.koppe.cuf.mail.server.smtp.SmtpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class of the system
 */
public class Main {
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	/**
	 * All running server instances
	 */
	private static final List<Server> runningInstances = new ArrayList<>();
	/**
	 * Indicates whether the system is running
	 */
	private static volatile boolean running = true;

	/**
	 * Main method of the system
	 * 
	 * @param args System arguments. Currently not used
	 */
	public static void main(String[] args) {
		LocalDateTime startup = LocalDateTime.now();
		logger.info("Starting mail server at {}", startup);

		try {
			initialize();
		} catch (ConfigurationException e) {
			logger.error("Exception during initialization", e);
			return;
		}

		try {
			initHttp();
		} catch (StartupException e) {
			logger.error("Exception during startup of http servers", e);
			return;
		}

		try {
			initSmtp();
		} catch (StartupException e) {
			logger.error("Exception during startup of smtp servers", e);
			return;
		}

		ThreadFactory f = Thread.ofVirtual().factory();
		runningInstances.forEach(s -> f.newThread(s).start());

		logger.info("Startup of mail server finished at {}", LocalDateTime.now());

		while (running) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.debug(e.getMessage(), e);
			}
		}
		logger.info("Shutting down system");
		runningInstances.forEach(s -> s.shutdown());
	}

	private static void initialize() throws ConfigurationException {
		try {
			ConfigInitializer.execute();
		} catch (ConfigurationException ex) {
			logger.error("Could not load configuration due to an exception", ex);
			throw ex;
		}

		// try {
		// HibernateFactory.buildSessionFactory();
		// } catch (StartupException ex) {
		// logger.error("Could not create connection to database, cancelling startup",
		// ex);
		// return;
		// }
	}

	private static void initHttp() throws StartupException {
		try {
			HttpInitializer.initializeEndpoints();
		} catch (StartupException ex) {
			logger.error("Could not initialize http server", ex);
			throw ex;
		}

		HttpServer https = new HttpServer(NetworkConfig.HTTPS_PORT, true);
		runningInstances.add(https);

		if (SecurityConfig.ALLOW_PLAIN) {
			logger.debug("Plain http messages are allowed");
			HttpServer http = new HttpServer(NetworkConfig.HTTP_PORT, false);
			runningInstances.add(http);
		}

		return;
	}

	private static void initSmtp() throws StartupException {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Could not determine hostname the server is running on.", e);
			throw new StartupException("Could not determine hostname the server is running on.", e);
		}
		SmtpServer smtp = new SmtpServer(hostname, NetworkConfig.SMTP_PORT);
		runningInstances.add(smtp);
	}

	public static void shutdown() {
		running = false;
	}
}
