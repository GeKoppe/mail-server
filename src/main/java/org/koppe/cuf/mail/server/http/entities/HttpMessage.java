package org.koppe.cuf.mail.server.http.entities;

import java.util.Map;

public interface HttpMessage {
    /**
     * Body of the request
     * 
     * @return Body of the request
     */
    public Body getBody();

    /**
     * Headers of the request
     * 
     * @return Headers of the request
     */
    public Map<String, String> getHeaders();

    public void setHeaders(Map<String, String> headers);

    /**
     * Method of the message
     * 
     * @return Method of the message
     */
    public Method getMethod();

    public void setMethod(Method method);

    /**
     * Protocol of the request including protocol version
     * 
     * @return Protocol of the request including protocol version
     */
    public String getProtocol();

    public void setProtocol(String protocol);
}
