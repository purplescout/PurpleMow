package se.purplescout.purplemow.onboard.backend.dao;

import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;

public interface ScheduleEventDAO extends GenericDAO<ScheduleEvent, Integer> {

	List<ScheduleEvent> getAllActiveEvents(Date start, Date end);
}
