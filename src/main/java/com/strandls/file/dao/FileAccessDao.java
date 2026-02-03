package com.strandls.file.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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

	public List<FileDownloads> getfilesList(Integer offset, Integer limit, Boolean deleted) {

		Session session = sessionFactory.openSession();
		List<FileDownloads> downloadLogList = new ArrayList<FileDownloads>();

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<FileDownloads> cr = cb.createQuery(FileDownloads.class);
		Root<FileDownloads> root = cr.from(FileDownloads.class);

		if (deleted != null) {
			cr.select(root).where(cb.equal(root.get("isDeleted"), deleted)).orderBy(cb.desc(root.get("id")));

		} else {
			cr.select(root).orderBy(cb.desc(root.get("id")));
		}

		try {
			Query<FileDownloads> query = session.createQuery(cr);

			if (offset != null) {
				query.setFirstResult(offset);
			}

			if (limit != null) {
				query.setMaxResults(limit);
			}

			downloadLogList = query.getResultList();

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			session.close();
		}

		return downloadLogList;
	}

	@SuppressWarnings("unchecked")
	public Long getTotalFileLogs(Boolean deleted) {

		String qry = "select count(id) from file_downloads";
		Session session = sessionFactory.openSession();

		try {
			if (deleted != null) {
				qry += " where is_deleted = :deleted";
			}
			Query<Number> query = session.createNativeQuery(qry);
			if (deleted != null) {
				query.setParameter("deleted", deleted);
			}
			return query.getSingleResult().longValue();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0L;
		} finally {
			session.close();
		}
	}

}
