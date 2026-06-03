package org.koppe.cuf.mail.server.common.events;

import org.koppe.cuf.mail.server.common.Event;
import org.koppe.cuf.mail.server.common.Session;
import org.koppe.cuf.mail.server.common.events.StatusChangeEvent.StatusChange;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StatusChangeEvent implements Event<Session, StatusChange> {
    @Getter
    private Session cause;
    @Getter
    private StatusChange information;

    public static enum StatusChange {
        START,
        WORKING,
        DONE;
    }
}
