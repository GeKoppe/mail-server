package org.koppe.cuf.mail.server.common.mail;

import java.io.BufferedReader;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

import org.koppe.cuf.mail.server.db.jpa.User;

/**
 * Represents current protocol context, including current state, reader and
 * writer for in- and outputstream etc.
 */
public interface Context<T extends State, C extends Command<T>> extends AutoCloseable {
    /**
     * Gets current state of communication
     * 
     * @return Current state of communication
     */
    public State getState();

    /**
     * Sets current state of communication
     * 
     * @param state Current state of communication
     */
    public void setState(T state);

    /**
     * Returns the mail of the current communication
     * 
     * @return mail of the current communication
     */
    public Mail getMail();

    /**
     * Sets mail of the current communication
     * 
     * @param mail mail of the current communication
     */
    public void setMail(Mail mail);

    /**
     * Returns the writer for the output stream of the communication socket
     * 
     * @return writer for the output stream of the communication socket
     */
    public Writer getWriter();

    /**
     * Sets writer for the output stream of the communication socket
     * 
     * @param writer writer for the output stream of the communication socket
     */
    public void setWriter(Writer writer);

    /**
     * Gets reader for the input stream of the communication socket
     * 
     * @return reader for the input stream of the communication socket
     */
    public BufferedReader getReader();

    /**
     * Sets reader for the input stream of the communication socket
     * 
     * @param reader reader for the input stream of the communication socket
     */
    public void setReader(BufferedReader reader);

    /**
     * Gets hostname of the system
     * 
     * @return hostname of the system
     */
    public String getHostname();

    /**
     * Sets hostname of the system
     * 
     * @param hostname hostname of the system
     */
    public void setHostname(String hostname);

    /**
     * Gets current arguments sent by the client
     * 
     * @return current arguments sent by the client
     */
    public Map<String, String> getArguments();

    /**
     * Sets current arguments sent by the client
     * 
     * @param arguments current arguments sent by the client
     */
    public void setArguments(Map<String, String> arguments);

    /**
     * Signalises, whether context is still active
     * 
     * @return True if context is still active
     */
    public boolean isActive();

    /**
     * Set to true, if context is still active
     * 
     * @param active true, if context is still active
     */
    public void setActive(boolean active);

    /**
     * Get client command
     * 
     * @return cient command
     */
    public C getClientCommand();

    /**
     * Set client command
     * 
     * @param command client command
     */
    public void setClientCommand(C command);

    /**
     * Gets socket of the socket
     * 
     * @return socket of the socket
     */
    public Socket getSocket();

    /**
     * Sets socket of the socket
     * 
     * @param socket socket of the socket
     */
    public void setSocket(Socket socket);

    public User getUser();

    public void setUser(User user);
}
