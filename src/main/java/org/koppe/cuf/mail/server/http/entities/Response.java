package org.koppe.cuf.mail.server.http.entities;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;

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

    /**
     * Factory method for creating http responses
     * 
     * @param <T> Type of the response body
     * @param c   Status code
     * @param m   Http status message
     * @param b   Response body
     * @param cl  Type of response body
     * @param h   Headers for the response
     * @return The initialized http response
     */
    public static <T> Response<T> of(int c, String m, ResponseBody<T> b, TypeReference<T> cl,
            Map<String, String> h) {
        return new Response<T>() {
            @Getter
            private int code = c;
            @Getter
            private String message = m;
            @Getter
            private ResponseBody<T> body = b;
            @Getter
            @Setter
            private Map<String, String> headers = h;
            @Getter
            @Setter
            private String protocol;
            @Getter
            @Setter
            private Method method;
        };
    }

    /**
     * Returns an http ok response
     * 
     * @param <T>       Type of the body
     * @param body      Body
     * @param type      Typereference to type of body
     * @param mediaType Mediatype of the response
     * @return The created response
     */
    public static <T> Response<T> ok(T body, TypeReference<T> type, MediaType mediaType) {
        return Response.of(HttpCode.OK.getCode(), HttpCode.OK.getInfo(), ResponseBody.of(body, type, mediaType), type,
                new HashMap<>());
    }

    /**
     * Returns an empty noContent response
     * 
     * @param <T> Type of the response
     * @return A no content http response
     */
    public static <T> Response<T> noContent() {
        return noContent(new HashMap<>());
    }

    /**
     * Returns an empty noContent response with set headers
     * 
     * @param <T>     Type of the response
     * @param headers Headers to set
     * @return A no content http response
     */
    public static <T> Response<T> noContent(Map<String, String> headers) {
        return Response.of(HttpCode.NO_CONTENT.getCode(), HttpCode.NO_CONTENT.getInfo(),
                ResponseBody.of(null, new TypeReference<T>() {

                }, MediaType.APPLICATION_JSON), new TypeReference<T>() {

                }, headers);
    }

    /**
     * Returns a standard unauthorized response
     * 
     * @return A default unauthorized resopnse
     */
    public static <T> Response<T> unauthorized() {
        return Response.of(HttpCode.UNAUTHORIZED.getCode(), HttpCode.UNAUTHORIZED.getInfo(),
                ResponseBody.of(null, null, MediaType.APPLICATION_JSON), null, new HashMap<>());
    }

    /**
     * Returns a parametrized unauthorized response
     * 
     * @param <T>  Type of the body
     * @param type Typereference for the type of the body
     * @return A parametrized unauthorized response
     */
    public static <T> Response<T> unauthorized(TypeReference<T> type) {
        return Response.of(HttpCode.UNAUTHORIZED.getCode(), HttpCode.UNAUTHORIZED.getInfo(),
                ResponseBody.of(null, type, MediaType.APPLICATION_JSON), type, new HashMap<>());
    }
}
