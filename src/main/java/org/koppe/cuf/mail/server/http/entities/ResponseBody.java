package org.koppe.cuf.mail.server.http.entities;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

public interface ResponseBody<T> extends Body {
    public T getObject();

    public MediaType getMediaType();

    /**
     * Creates a response body
     * 
     * @param <T>       Type of the object the response body represents
     * @param object    Object the response body represents
     * @param clazz     Type of the response body
     * @param mediaType Media type of the response body
     * @return Created response body
     */
    public static <T> ResponseBody<T> of(T object, TypeReference<T> clazz, String mediaType) {
        return of(object, clazz, MediaType.ofValue(mediaType));
    }

    /**
     * Creates a response body
     * 
     * @param <T> Type of the object the response body represents
     * @param o   Object the response body represents
     * @param c   Type of the response body
     * @param m   Media type of the response body
     * @return Created response body
     */
    public static <T> ResponseBody<T> of(T o, TypeReference<T> c, MediaType m) {
        return new ResponseBody<T>() {
            @Getter
            private String string;
            @Getter
            private final T object = o;
            @Getter
            private final MediaType mediaType = m;
        };
    }

    public static <T> ResponseBody<T> of(String s, T o, TypeReference<T> c, MediaType m) {
        return new ResponseBody<T>() {
            @Getter
            private String string = s;
            @Getter
            private final T object = o;
            @Getter
            private final MediaType mediaType = m;
        };
    }
}
