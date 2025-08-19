package com.strandls.file.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.strandls.file.model.UploadedFileMetadata;

import jakarta.inject.Inject;

public class UploadedMetaDataDao extends AbstractDao<UploadedFileMetadata, Long> {

	@Inject
	public UploadedMetaDataDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public UploadedFileMetadata findById(Long id) {
		Session session = sessionFactory.openSession();
		UploadedFileMetadata entity;
		try {
			entity = session.get(UploadedFileMetadata.class, id);
		} catch (Exception e) {
			throw e;
		} finally {
			session.close();
		}
		return entity;
	}
}
