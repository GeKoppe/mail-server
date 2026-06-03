package org.koppe.cuf.mail.server.http.entities;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Response message of a http communication
 */
public interface Response<T> extends HttpMessage {
    /**
     * Response code
     * 
     * @return Response code
     */
    public int getCode();

    /**
     * Status message
     * 
     * @return Status mussage
     */
    public String getMessage();

    /**
     * Response body
     * 
     * @return Response body
     */
    @Override
    public ResponseBody<T> getBody();

    public static <T> Response<T> of(int c, String m, ResponseBody<T> b, Class<T> cl) {
        return new Response<T>() {
            @Getter
            private int code = c;
            @Getter
            private String message = m;
            @Getter
            private ResponseBody<T> body = b;
            @Getter
            @Setter
            private Map<String, String> headers;
            @Getter
            @Setter
            private String protocol;
            @Getter
            @Setter
            private Method method;
        };
    }
}
