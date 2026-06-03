package org.koppe.cuf.mail.server.http.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MediaType {
    APPLICATION_JSON("application/json"),
    APPLICATION_JSON_UTF8("application/json;charset=utf8");

    @Getter
    private final String value;

    public static MediaType ofValue(String val) {
        for (var x : values()) {
            if (x.getValue().equals(val))
                return x;
        }
        return null;
    }
}
