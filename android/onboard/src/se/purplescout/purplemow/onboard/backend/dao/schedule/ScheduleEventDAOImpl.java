package se.purplescout.purplemow.onboard.backend.dao.schedule;

import java.sql.SQLException;

import se.purplescout.purplemow.onboard.backend.dao.GenericDAOImpl;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;

import com.google.inject.Inject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;

public class ScheduleEventDAOImpl extends GenericDAOImpl<ScheduleEvent, Integer> implements ScheduleEventDAO {

	private RuntimeExceptionDao<ScheduleEvent, Integer> dao;

	@Inject
	public ScheduleEventDAOImpl(ConnectionSource connectionSource) throws SQLException {
		this.dao = RuntimeExceptionDao.<ScheduleEvent, Integer>createDao(connectionSource, ScheduleEvent.class);
	}

	@Override
	protected RuntimeExceptionDao<ScheduleEvent, Integer> getDAO() {
		return dao;
	}
}
