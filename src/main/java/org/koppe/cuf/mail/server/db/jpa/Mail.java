package org.koppe.cuf.mail.server.db.jpa;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;

@Entity
@Table(name = "mails")
@Getter
@Setter
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode
public class Mail implements BoxElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    @Include
    private Long id;

    @Column(name = "from", unique = false, nullable = false)
    @Include
    private String from;

    @Column(name = "subject", unique = false, nullable = true)
    private String subject;

    @Column(name = "received", unique = false, nullable = false)
    private LocalDateTime received;

    @Column(name = "read", unique = false, nullable = false)
    private Boolean read;

    @Column(name = "deleted", unique = false, nullable = false)
    private Boolean deleted;

    @OneToMany(mappedBy = "mail")
    private List<MailMetadata> metadata;

    @ManyToMany(mappedBy = "mails")
    private List<User> user;

}
