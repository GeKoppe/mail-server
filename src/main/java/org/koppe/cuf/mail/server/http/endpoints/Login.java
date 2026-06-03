package org.koppe.cuf.mail.server.http.endpoints;

import java.util.Map;

import org.koppe.cuf.mail.server.http.dto.LoginDto;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.koppe.cuf.mail.server.http.entities.MediaType;
import org.koppe.cuf.mail.server.http.entities.Method;
import org.koppe.cuf.mail.server.http.entities.Path;
import org.koppe.cuf.mail.server.http.entities.Request;
import org.koppe.cuf.mail.server.http.entities.Response;
import org.koppe.cuf.mail.server.http.entities.ResponseBody;

import lombok.Getter;

public class Login implements Endpoint<LoginDto, Void> {
    @Getter
    private Method method = Method.POST;
    @Getter
    private Path path = Path.of("/login", Map.of());
    @Getter
    private Class<LoginDto> inputType;
    @Getter
    private Class<Void> outputType;
    @Getter
    private boolean authenticated;
    @Getter
    private boolean asList;

    @Override
    public Response<Void> handle(Request<LoginDto> i) {
        return Response.of(200, "Succeded", ResponseBody.of(null, outputType, MediaType.APPLICATION_JSON), outputType);
    }
}
