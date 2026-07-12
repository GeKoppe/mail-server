package org.koppe.cuf.mail.server.smtp.entities;

import java.util.HashMap;
import java.util.Map;

public class DNSCache {
    private static final Map<String, MXRecord> dnsCache = new HashMap<>();

    /**
     * 
     * @param domain
     * @return
     */
    public static MXRecord get(String domain) {
        return dnsCache.getOrDefault(domain, null);
    }

    public static void put(String domain, MXRecord value) {
        dnsCache.put(domain, value);
    }
}
