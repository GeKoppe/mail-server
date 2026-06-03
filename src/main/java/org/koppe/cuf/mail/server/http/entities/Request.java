package org.koppe.cuf.mail.server.http.entities;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public interface Request<T> extends HttpMessage {
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestBody<T> getBody();

    /**
     * Sets body of the request
     * 
     * @param body Body of the request
     */
    public void setBody(RequestBody<T> body);

    /**
     * Requested resource path
     * 
     * @return Requested resource path
     */
    public String getPath();

    /**
     * Sets path of the request
     * 
     * @param path path of the request
     */
    public void setPath(String path);

    /**
     * Query of the request
     * 
     * @return Query of the request
     */
    public Map<String, String> getQuery();

    /**
     * Sets Query of the request
     * 
     * @param query Query of the request
     */
    public void setQuery(Map<String, String> query);

    /**
     * Creates new empty Request instance
     * 
     * @param <T>  Type of the request body
     * @param type Type of the body
     * @return An empty request instance
     */
    public static <T> Request<T> empty(Class<T> type) {
        return new Request<T>() {
            @Getter
            @Setter
            private String path;
            @Getter
            @Setter
            private RequestBody<T> body;
            @Getter
            @Setter
            private Map<String, String> query;
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
