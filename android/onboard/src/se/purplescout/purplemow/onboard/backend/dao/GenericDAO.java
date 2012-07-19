package se.purplescout.purplemow.onboard.backend.dao;

import java.util.Collection;
import java.util.List;

public interface GenericDAO<E, PK> {

	public void create(E entity);

	public void delete(E entity);

	public void delete(Collection<E> entitis);

	public void update(E entity);

	public E findById(PK id);

	public List<E> listAll();
}
