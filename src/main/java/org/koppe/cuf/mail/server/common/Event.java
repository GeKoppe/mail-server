package org.koppe.cuf.mail.server.common;

import lombok.Getter;

/**
 * Events
 */
public interface Event<C, I> {
    /**
     * Cause of the event
     * 
     * @return Cause of the event
     */
    public C getCause();

    /**
     * Information relayed by the event
     * 
     * @return Information relayed by the event
     */
    public I getInformation();

    /**
     * Factory method for creating events
     * 
     * @param <C> Type of event cause
     * @param <I> Type of event information
     * @param c   Event cause
     * @param i   Event information
     * @return Built event
     */
    public static <C, I> Event<C, I> of(C c, I i) {
        return new Event<C, I>() {
            @Getter
            private C cause = c;
            @Getter
            private I information = i;
        };
    }
}
