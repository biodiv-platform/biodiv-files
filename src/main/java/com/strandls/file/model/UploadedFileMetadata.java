package com.strandls.file.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uploaded_file_metadata")
public class UploadedFileMetadata implements Serializable {

	/** */
	private static final long serialVersionUID = -4561731591816339242L;

	public UploadedFileMetadata(Long userId, String uploadedFileOriginalName, String uploadedFileRenamed, String type,
			Date movementDate) {
		this.userId = userId;
		this.fileOriginalName = uploadedFileOriginalName;
		this.fileNewName = uploadedFileRenamed;
		this.type = type;
		this.movementDate = movementDate;
	}

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "file_original_name", nullable = false)
	private String fileOriginalName;

	@Column(name = "file_new_name", nullable = false)
	private String fileNewName;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "movement_date", nullable = false)
	private Date movementDate;
}
