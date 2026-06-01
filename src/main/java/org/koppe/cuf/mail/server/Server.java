package org.koppe.cuf.mail.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadFactory;

import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.db.HibernateFactory;
import org.koppe.cuf.mail.server.imap.ImapServer;
import org.koppe.cuf.mail.server.smtp.SmtpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	public static void main(String[] args) {
		LocalDateTime startup = LocalDateTime.now();
		logger.info("Starting mail server at {}", startup);

		try {
			HibernateFactory.buildSessionFactory();
		} catch (StartupException ex) {
			logger.error("Could not create connection to database, cancelling startup", ex);
			return;
		}

		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Could not determine hostname the server is running on.", e);
			return;
		}

		SmtpServer smtp = new SmtpServer(hostname, 2525);
		ImapServer imap = new ImapServer(6666, hostname);

		ThreadFactory f = Thread.ofVirtual().factory();

		f.newThread(smtp).start();
		f.newThread(imap).start();

		logger.info("Startup of mail server finished at {}", LocalDateTime.now());

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.debug(e.getMessage(), e);
			}
		}
	}
}
