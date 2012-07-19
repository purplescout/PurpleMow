package se.purplescout.purplemow.onboard.backend.dao.schedule;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.backend.dao.GenericDAOImpl;
import se.purplescout.purplemow.onboard.backend.dao.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;

public class ScheduleEventDAOImpl extends GenericDAOImpl<ScheduleEvent, Integer> implements ScheduleEventDAO {

	private RuntimeExceptionDao<ScheduleEvent, Integer> dao;

	public ScheduleEventDAOImpl(ConnectionSource connectionSource) throws SQLException {
		this.dao = RuntimeExceptionDao.<ScheduleEvent, Integer>createDao(connectionSource, ScheduleEvent.class);
	}

	@Override
	protected RuntimeExceptionDao<ScheduleEvent, Integer> getDAO() {
		return dao;
	}

	@Override
	public List<ScheduleEvent> getAllActiveEvents(Date start, Date end) {
		// TODO Auto-generated method stub
		return null;
	}
}
