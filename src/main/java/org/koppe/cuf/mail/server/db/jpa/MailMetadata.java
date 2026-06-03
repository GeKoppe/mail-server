package org.koppe.cuf.mail.server.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "mail_metadata")
@RequiredArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode
public class MailMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metadata_id", nullable = false, unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mail_id", nullable = false)
    private Mail mail;

    @Column(name = "meta_key", unique = false, nullable = false)
    private String key;

    @Column(name = "meta_value", unique = false, nullable = false)
    private String value;
}
