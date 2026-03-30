/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.file.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author sethuraman
 */
@Entity
@Table(name = "file_downloads")
@XmlRootElement
public class FileDownloads implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;

	@Basic(optional = false)
	@NotNull
	@Column(name = "date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@Basic(optional = false)
	@NotNull
	@Column(name = "file_name")
	private String fileName;

	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private FileDownloadCredentials userId;

	@Column(name = "created_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name = "status")
	private String status = "READY";

	@Column(name = "is_deleted", columnDefinition = "boolean default false")
	private Boolean isDeleted;

	public FileDownloads() {
	}

	public FileDownloads(Integer id) {
		this.id = id;
	}

	public FileDownloads(Integer id, Date date, String fileName) {
		this.id = id;
		this.date = date;
		this.fileName = fileName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public FileDownloadCredentials getUserId() {
		return userId;
	}

	public void setUserId(FileDownloadCredentials userId) {
		this.userId = userId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof FileDownloads)) {
			return false;
		}
		FileDownloads other = (FileDownloads) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.strandls.hibernateormdemo.model.FileDownloads[ id=" + id + " ]";
	}
}
