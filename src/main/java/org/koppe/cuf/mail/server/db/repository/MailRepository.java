package org.koppe.cuf.mail.server.db.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.koppe.cuf.mail.server.db.jpa.Mail;
import org.koppe.cuf.mail.server.db.jpa.Mail_;
import org.koppe.java.expansion.validation.ValidationUtils;

public class MailRepository extends JpaRepository<Mail, Long> {

    public MailRepository() {
        super(Mail.class);
    }

    public Optional<Mail> findById(long id) {
        return super.findById(id);
    }

    public List<Mail> findByGuid(String guid) {
        return ValidationUtils.checkNotNullOrEmpty(guid) ? super.findBy(Mail_.guid, guid) : new ArrayList<>();
    }

    public Mail update(Mail mail) throws Exception {
        if (!(ValidationUtils.checkNotNullOrEmpty(mail) && ValidationUtils.checkNotNullOrEmpty(mail.getId()))) {
            return null;
        }

        return super.update(mail, mail.getId(), (m, x) -> {
            m.setDeleted(x.getDeleted());
            m.setFilePath(x.getFilePath());
            m.setFrom(x.getFrom());
            m.setGuid(x.getGuid());
            m.setMetadata(x.getMetadata());
            m.setRead(x.getRead());
            m.setReceived(x.getReceived());
            m.setSubject(x.getSubject());
            m.setUser(x.getUser());
        });
    }

}
