package se.purplescout.purplemow.onboard.backend.service.schedule;

import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;

public interface ScheduleService {

	List<ScheduleEventDTO> getScheduleForWeek(Date date);

	List<Date> getDatesForWeek(Date date);

	void save(List<ScheduleEventDTO> dtos);

	void initScheduler();

	void save(ScheduleEventDTO scheduleEvent);
}
