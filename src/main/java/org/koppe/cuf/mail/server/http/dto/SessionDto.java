package org.koppe.cuf.mail.server.http.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * DTO containing session information
 */
@RequiredArgsConstructor
@Getter
@Setter
public class SessionDto {
    private final String jwt;
    private final String refresh;
}
