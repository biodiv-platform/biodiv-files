package com.strandls.file.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "firebase_tokens")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "user" })
public class FirebaseTokens implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Column(name = "token")
	private String token;

	public FirebaseTokens() {
		super();
	}

	public FirebaseTokens(User user, String token) {
		super();
		this.user = user;
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User userId) {
		this.user = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "FirebaseTokens [id=" + id + ", userId=" + user + ", token=" + token + "]";
	}
}
