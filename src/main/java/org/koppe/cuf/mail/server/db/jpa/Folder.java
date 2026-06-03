package org.koppe.cuf.mail.server.db.jpa;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "folders", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "folder_name", "parent_id", "owner_id" }, name = "parent_folder_owner")
})
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Folder implements BoxElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_id", unique = false, nullable = true)
    @Fetch(FetchMode.JOIN)
    private Folder parent;

    @Column(name = "folder_name", unique = false, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id", unique = false, nullable = false)
    private User owner;
}
