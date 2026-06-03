package org.koppe.cuf.mail.server.http.entities;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public enum Method {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    @Getter
    private final String value;

    public static @Nullable Method ofValue(@NotNull String value) {
        for (var x : Method.values()) {
            if (x.getValue().equals(value))
                return x;
        }
        return null;
    }
}
