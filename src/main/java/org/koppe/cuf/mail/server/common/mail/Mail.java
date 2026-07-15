package org.koppe.cuf.mail.server.common.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Represents a single mail
 */
@Data
public class Mail {
    /**
     * Mail headers
     */
    private Map<String, String> header = new HashMap<>();
    /**
     * Sender of the mail
     */
    private String from;
    /**
     * Recipients of the mail
     */
    private List<String> to = new ArrayList<>();
    /**
     * Carbon copy recipients of the mail
     */
    private List<String> cc = new ArrayList<>();
    /**
     * Blind carbon copy recipients of the mail
     */
    private List<String> bcc = new ArrayList<>();
    /**
     * Subject of the mail
     */
    private String subject;
    /**
     * Body of the mail
     */
    private String body;

    public String toFileString() {
        StringBuilder sb = new StringBuilder();

        header.entrySet().forEach(x -> {
            sb.append(x.getKey())
                    .append(": ")
                    .append(x.getValue())
                    .append("\r\n");
        });
        sb.append("\r\n")
                .append(body);

        return sb.toString();
    }
}
