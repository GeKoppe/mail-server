package org.koppe.cuf.mail.server.smtp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.koppe.cuf.mail.server.common.mail.Mail;
import org.koppe.cuf.mail.server.common.mail.WritingUtils;
import org.koppe.cuf.mail.server.smtp.entities.DNSCache;
import org.koppe.cuf.mail.server.smtp.entities.MXRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

/**
 * Used for sending smtp messages
 */
@RequiredArgsConstructor
public class SmtpSender {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(SmtpSender.class);
    private final boolean isProd;
    private final List<String> unreachableHosts = new ArrayList<>();

    // #region send
    /**
     * Sends the given mail
     * 
     * @param mail Mail to send
     */
    public void send(Mail mail) {
        logger.info("Sending mail");

        List<MXRecord> mxRecords = getMXRecords(mail);
        logger.info("Retrieved relevant mx records");

        if (!isProd) {
            logger.warn("DEVELOPMENT SWITCH ACTIVE, ONLY SENDING TO DEV EMAILS");
            mxRecords = new ArrayList<>();
            mxRecords.add(modifyDev(mail));
        }

        sendMail(mail, mxRecords);
    }

    /**
     * Modifies the mail object for development purposes and returns an mxrecord
     * pointing to localhost.
     * 
     * @param mail Mail to modify
     * @return An MX record pointing to localhost
     */
    private MXRecord modifyDev(Mail mail) {
        mail.getTo().clear();
        mail.getTo().add("test@test.com");
        mail.getCc().clear();
        mail.getBcc().clear();

        return new MXRecord(0, "localhost");
    }

    /**
     * Retrieves all required mx records
     * 
     * @param mail Mail to send
     * @return List of all required mx records
     */
    private List<MXRecord> getMXRecords(Mail mail) {
        logger.debug("Searching for mx record");
        logger.debug("Extracting receiving domains");

        List<String> domains = extractDomains(mail);
        logger.debug("Extracted {} domains", domains.size());

        List<MXRecord> domainsToAddress = new ArrayList<>();
        for (var x : domains) {
            domainsToAddress.add(getRecordForDomain(x));
        }

        return domainsToAddress.stream().filter(s -> s != null).toList();
    }

    /**
     * Extracts domains from all recipients in the mail.
     * 
     * @param mail Mail to extract domains from
     * @return List of all domains in mail
     */
    private List<String> extractDomains(Mail mail) {
        List<String> allRecipients = new ArrayList<>();
        allRecipients.addAll(mail.getTo());
        allRecipients.addAll(mail.getCc());
        allRecipients.addAll(mail.getBcc());
        return allRecipients.stream().filter(s -> s != null && s.contains("@"))
                .map(s -> s.substring(s.indexOf("@") + 1))
                .collect(Collectors.toSet()).stream().toList();
    }

    /**
     * First tries to look up the
     * {@link org.koppe.cuf.mail.server.smtp.entities.DNSCache} for
     * {@link org.koppe.cuf.mail.server.smtp.entities.MXRecord} for the given
     * domain. If no such entry exists, actual dns lookup is executed
     * 
     * @param domain Domain to get mx record for.
     * @return The mx record for the given domain
     */
    private MXRecord getRecordForDomain(String domain) {
        logger.debug("Getting mx record for domain {}", domain);
        MXRecord rec = DNSCache.get(domain);
        if (rec != null) {
            return rec;
        }

        rec = lookup(domain);
        DNSCache.put(domain, rec);
        return rec;
    }

    /**
     * Executes dns lookup for the given domain and returns the mx record with
     * highest priority related to it.
     * 
     * @param domain Domain to execute dns lookup for.
     * @return The matching mx record.
     */
    private MXRecord lookup(String domain) {
        logger.debug("Looking up mx record for domain {}", domain);
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

        try {
            DirContext ctx = new InitialDirContext(env);
            Attributes att = ctx.getAttributes(domain, new String[] { "MX" });

            NamingEnumeration<?> records = att.get("MX").getAll();
            List<MXRecord> recs = new ArrayList<>();
            while (records.hasMore()) {
                String record = records.next().toString();
                recs.add(new MXRecord(Integer.parseInt(record.split(" ")[0]), record.split(" ")[1]));
            }

            recs.sort(Comparator.comparingInt(MXRecord::priority));
            return recs.get(0);
        } catch (NamingException e) {
            logger.warn("Could not lookup mx record due to exception {}", e);
            return null;
        }
    }

    private void sendMail(Mail mail, List<MXRecord> records) {
        logger.info("Starting to send mail");
        for (var r : records) {
            send(mail, r);
        }
    }

    private void send(Mail mail, MXRecord mx) {
        try (Socket socket = new Socket(mx.host(), isProd ? 25 : 3000)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            logger.debug("Initialised socket in- and output stream");

            if (!sendEhloAndHandleStartTls(socket, reader, writer)) {
                logger.warn("Mail could not be sent");
                unreachableHosts.add(mx.host());
                reader.close();
                writer.close();
                return;
            }

            if (!sendSmtp(mail, reader, writer)) {
                logger.warn("Mail could not be sent");
                unreachableHosts.add(mx.host());
                reader.close();
                writer.close();
                return;
            }
        } catch (UnknownHostException e) {
            logger.error("Could not connect to host {}", mx.host(), e);
            unreachableHosts.add(mx.host());
        } catch (IOException e) {
            logger.error("Could not send mail to host {}", mx.host(), e);
            unreachableHosts.add(mx.host());
        }
        return;
    }

    private boolean sendSmtp(Mail mail, BufferedReader reader, PrintWriter writer) {
        WritingUtils.write(writer, "MAIL FROM:<" + mail.getFrom() + ">");
        for (var x : mail.getTo()) {
            WritingUtils.write(writer, "RCPT TO:<" + x + ">");
        }
        WritingUtils.write(writer, "DATA");

        for (var x : mail.getCc()) {
            WritingUtils.write(writer, "Cc: " + x);
        }

        for (var x : mail.getBcc()) {
            WritingUtils.write(writer, "Bcc: " + x);
        }

        WritingUtils.write(writer, "Subject: " + mail.getSubject());
        for (var x : mail.getHeader().entrySet()) {
            WritingUtils.write(writer, x.getKey() + ": " + x.getValue());
        }

        WritingUtils.write(writer, "");

        BufferedReader r = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(mail.getBody().getBytes())));

        try {
            String line = null;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("."))
                    line = "." + line;
                WritingUtils.write(writer, line);
            }
            WritingUtils.write(writer, ".");
            r.close();
        } catch (Exception ex) {
            logger.warn("Could not send mail", ex);
            return false;
        }

        return true;
    }

    // #region ehlo and starttls
    /**
     * Does the ehlo and starttls flow
     * 
     * @param socket
     * @param reader
     * @param writer
     * @return
     */
    private boolean sendEhloAndHandleStartTls(Socket socket, BufferedReader reader, PrintWriter writer) {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Could not determine local host name");
            hostname = "client.local";
        }
        WritingUtils.write(writer, "EHLO " + hostname);

        String line = null;
        boolean startTls = false;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains("STARTTLS"))
                    startTls = true;
                if (!line.startsWith("250-"))
                    break;
            }
        } catch (IOException e) {
            logger.warn("Could not read line from socket", e);
            return false;
        }

        if (startTls) {
            logger.info("Server expects start tls, start wrapping");
            try {
                socket = wrapSocket(socket);
            } catch (IOException e) {
                logger.warn("Could not wrap socket in tls due to exception", e);
                return false;
            }
        }

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            logger.warn("Could not initialise reader and writer for new socket");
            return false;
        }

        WritingUtils.write(writer, "EHLO " + hostname);
        try {
            while ((line = reader.readLine()) != null) {
                if (line.contains("STARTTLS"))
                    startTls = true;
                if (!line.startsWith("250-"))
                    break;
            }
        } catch (IOException e) {
            logger.warn("Could not read line from socket", e);
            return false;
        }

        return true;
    }

    // #region wrap socket
    private Socket wrapSocket(Socket socket) throws IOException {
        logger.debug("Wrapping socket {}", socket);
        SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(
                socket, socket.getChannel().getRemoteAddress().toString(), isProd ? 25 : 3000, true);

        sslSocket.setUseClientMode(true);
        sslSocket.startHandshake();
        logger.debug("Wrapped socket in tls");

        return sslSocket;
    }
}
