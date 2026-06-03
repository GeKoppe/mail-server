package org.koppe.cuf.mail.server.http;

import org.koppe.cuf.mail.server.http.entities.Method;

record FirstLine(String resource, Method method, String protocol) {
}
