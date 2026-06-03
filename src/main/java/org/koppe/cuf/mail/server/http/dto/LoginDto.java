package org.koppe.cuf.mail.server.http.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class LoginDto {
    @Getter
    @Setter
    private String user;
    @Getter
    @Setter
    private String password;
}
