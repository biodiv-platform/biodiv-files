package com.strandls.file.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.file.model.FileDownloads;

public class FileAccessDao extends AbstractDao<FileDownloads, Long> {

	private static final Logger logger = LoggerFactory.getLogger(FileAccessDao.class);

	@Inject
	protected FileAccessDao(SessionFactory sessionFactory) {
		super(sessionFactory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FileDownloads findById(Long id) {
		Session session = sessionFactory.openSession();
		FileDownloads entity = null;
		try {
			entity = session.get(FileDownloads.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<FileDownloads> findByFileName(String fileName) {
		String qry = "from FileDownloads where fileName = :fileName";
		Session session = sessionFactory.openSession();
		List<FileDownloads> result = null;
		try {
			Query<FileDownloads> query = session.createQuery(qry);
			query.setParameter("fileName", fileName);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

}
