package org.koppe.cuf.mail.server.http.entities;

/**
 * Represents an http endpoint. All classes extending this interface are
 * searched for by the reflections library and instantiated automatically.
 */
public interface Endpoint<I, O> {
    /**
     * Returns method the endpoint can process
     * 
     * @return method the endpoint can process
     */
    public Method getMethod();

    /**
     * Path of the endpoint
     * 
     * @return Path of the endpoint
     */
    public Path getPath();

    /**
     * Type of body this endpoint expects
     * 
     * @return Type of body this endpoint expects
     */
    public Class<I> getInputType();

    /**
     * Type of the reponse body
     * 
     * @return Type of the reponse body
     */
    public Class<O> getOutputType();

    /**
     * Actual logic behind the endpoint
     * 
     * @param i Clint request
     * @return Server response
     */
    public Response<O> handle(Request<I> i);

    /**
     * If true, client needs to be authenticated for this endpoint
     * 
     * @return true, if client needs to be authenticated for this endpoint
     */
    public boolean isAuthenticated();

    public boolean isAsList();
}
