package org.koppe.cuf.mail.server.http.entities;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.Getter;

/**
 * Representation of a request body
 */
public interface RequestBody<T> extends Body {
    // #region parse
    /**
     * Parses the values in the inputstream to it's object representation
     * 
     * @return object representation of the input stream
     */
    public T getObject();

    /**
     * Creates a new RequestBody instance.
     * 
     * @param <T>   Type of the entity the request body should represent
     * @param body  String representation of the body
     * @param clazz Type of the entity the request body should represent
     * @return The created request body
     */
    public static <T> RequestBody<T> of(String body, TypeReference<T> clazz) {
        return new RequestBody<T>() {
            /**
             * Value of the inputstream
             */
            @Getter
            private String string = body;
            /**
             * Object representation of the body
             */
            @Getter
            private T object = null;
        };
    }

    /**
     * Creates a new RequestBody instance.
     * 
     * @param <T>   Type of the entity the request body should represent
     * @param o     Input stream from the client
     * @param clazz Type of the entity the request body should represent
     * @return The created request body
     */
    public static <T> RequestBody<T> of(T o, TypeReference<T> clazz) {
        return new RequestBody<T>() {
            /**
             * String representation of the body
             */
            @Getter
            private String string = null;
            /**
             * Object represetnation of the body
             */
            @Getter
            private T object = o;
        };
    }

    public static <T> RequestBody<T> of(T o, String s, TypeReference<T> clazz) {
        return new RequestBody<T>() {
            @Getter
            private String string = s;
            @Getter
            private T object = o;
        };
    }

    public static <T> RequestBody<T> empty(TypeReference<T> clazz) {
        return new RequestBody<T>() {
            @Getter
            private String string = null;
            @Getter
            private T object = null;
        };
    }
}
