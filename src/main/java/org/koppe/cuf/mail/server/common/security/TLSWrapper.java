package org.koppe.cuf.mail.server.common.security;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for wrapping sockets in tls
 */
public class TLSWrapper {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(TLSWrapper.class);

    /**
     * Wraps the socket in TLS
     * 
     * @param socket Socket to wrap
     * @return The wrapped socket or null, if socket could not be wrapped
     */
    public static Socket wrapTls(Socket socket, boolean clientMode) {
        logger.debug("Wrapping socket {} in tls");
        try {
            SSLSocket tls = (SSLSocket) ((SSLSocketFactory) (clientMode ? SSLSocketFactory.getDefault()
                    : TLSContext.getInstance().getSocketFactory()))
                    .createSocket(socket,
                            socket.getInputStream(),
                            true);
            tls.setUseClientMode(clientMode);
            tls.startHandshake();
            logger.debug("Socket wrapped and handshake executed");
            return tls;
        } catch (IOException | StartupException e) {
            logger.error("Could not build tls socket due to exception", e);
            return null;
        }
    }
}
