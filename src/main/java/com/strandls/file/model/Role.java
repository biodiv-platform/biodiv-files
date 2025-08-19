package com.strandls.file.model;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "users" })
@Schema(name = "Role", description = "Role entity representing user authorities")
public class Role implements Serializable {

	private static final long serialVersionUID = 6401648706578439017L;

	@Id
	@Column(name = "id")
	@Schema(description = "Unique identifier of the Role", example = "1", required = true)
	private Long id;

	@Column(name = "authority")
	@Schema(description = "Authority string representing the role", example = "ROLE_ADMIN", required = true)
	private String authority;

	@ManyToMany(mappedBy = "roles")
	@JsonIgnoreProperties("roles") // Prevent cyclic serialization
	private Set<User> users;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
}
