package org.koppe.cuf.mail.server.http.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public enum HttpCode {
    OK(200, "OK"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    SERVER_ERROR(500, "Internal Server Error");

    @Getter
    private final int code;
    @Getter
    private final String info;

    public static HttpCode ofCode(int code) {
        for (var x : values()) {
            if (x.code == code)
                return x;
        }
        return SERVER_ERROR;
    }
}