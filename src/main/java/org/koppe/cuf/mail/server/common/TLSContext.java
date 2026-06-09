package org.koppe.cuf.mail.server.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.config.SecurityConfig;

public class TLSContext {
    /**
     * SSL context
     */
    private static SSLContext instance = null;

    /**
     * Returns the ssl context instance. If it's not initialized, the instance is
     * initialized beforehand.
     * 
     * @return SSL Context instance
     * @throws StartupException If ssl context could not be initialized
     */
    public static SSLContext getInstance() throws StartupException {
        if (instance == null) {
            try {
                buildContext();
            } catch (Exception e) {
                throw new StartupException("Could not initialise SSL context", e);
            }
        }
        return instance;
    }

    private static void buildContext() throws Exception {
        KeyStore kst = KeyStore.getInstance("JKS");
        try (InputStream is = new FileInputStream(SecurityConfig.KEYSTORE_PATH)) {
            kst.load(is, SecurityConfig.KEYSTORE_PASS.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(kst, SecurityConfig.KEYSTORE_PASS.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(kst);
        instance = SSLContext.getInstance("TLS");
        instance.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }
}
