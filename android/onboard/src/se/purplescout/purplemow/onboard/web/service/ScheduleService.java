package se.purplescout.purplemow.onboard.web.service;

import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.dto.ScheduleEventDTO;

public interface ScheduleService {

	List<Date> getDatesForWeek(Date date);

	List<ScheduleEventDTO> getScheduleForWeek(Date date);
}
