package org.koppe.cuf.mail.server.db.jpa;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode
public class User {
	@Id
	@Column(name = "user_id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "username", nullable = false, unique = true)
	private String name;

	@Column(name = "user_mail", nullable = false, unique = true)
	private String mail;

	@Column(name = "password", nullable = false, unique = false)
	private String pw;

	@Column(name = "created", nullable = false, unique = false)
	private LocalDate created;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	@JoinTable(name = "user_mails", joinColumns = {
			@JoinColumn(name = "user_id", nullable = false, unique = false)
	}, inverseJoinColumns = {
			@JoinColumn(name = "mail_id", nullable = false, unique = false)
	}, indexes = {
			@Index(columnList = "user_id")
	})
	private List<Mail> mails;

	@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<Folder> folders;
}
