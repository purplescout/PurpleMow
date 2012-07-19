package se.purplescout.purplemow.onboard.backend.dao;

import java.util.Collection;
import java.util.List;

import com.j256.ormlite.dao.RuntimeExceptionDao;

public abstract class GenericDAOImpl<E, PK> implements GenericDAO<E, PK> {

	protected abstract RuntimeExceptionDao<E, PK> getDAO();

	@Override
	public void create(E entity) {
		getDAO().create(entity);
	}

	@Override
	public void delete(E entity) {
		getDAO().delete(entity);
	}

	@Override
	public void delete(Collection<E> entitis) {
		getDAO().delete(entitis);
	}

	@Override
	public void update(E entity) {
		getDAO().update(entity);
	}

	@Override
	public E findById(PK id) {
		return getDAO().queryForId(id);
	}

	@Override
	public List<E> listAll() {
		return getDAO().queryForAll();
	}
}
