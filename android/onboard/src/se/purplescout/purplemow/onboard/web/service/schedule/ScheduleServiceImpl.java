package se.purplescout.purplemow.onboard.web.service.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Log;

import se.purplescout.purplemow.onboard.backend.dao.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;
import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import se.purplescout.purplemow.onboard.web.service.ScheduleService;

public class ScheduleServiceImpl implements ScheduleService {

	private ScheduleEventDAO scheduleEntryDAO;

	public ScheduleServiceImpl(ScheduleEventDAO scheduleEntryDAO) {
		this.scheduleEntryDAO = scheduleEntryDAO;
	}

	@Override
	public List<ScheduleEventDTO> getScheduleForWeek(Date date) {
		return createDTOs(scheduleEntryDAO.listAll());
	}

	private List<ScheduleEventDTO> createDTOs(List<ScheduleEvent> entities) {
		List<ScheduleEventDTO> dtos = new ArrayList<ScheduleEventDTO>();
		for (ScheduleEvent entity : entities) {
			dtos.add(createDTO(entity));
		}

		return dtos;
	}

	private ScheduleEventDTO createDTO(ScheduleEvent entity) {
		ScheduleEventDTO dto = new ScheduleEventDTO();
		dto.setChanged(false);
		dto.setFromDB(true);
		dto.setId(entity.getId());
		dto.setInterval(entity.getInterval());
		dto.setStartDate(entity.getStartDate());
		dto.setStopDate(entity.getStopDate());
		dto.setType(entity.getType());
		dto.setActive(entity.isActive());

		return dto;
	}

	@Override
	public List<Date> getDatesForWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		List<Date> weekDates = new ArrayList<Date>();
		for (int i = 0; i < 7; i++) {
			weekDates.add(cal.getTime());
			cal.add(Calendar.DATE, 1);
		}
		
		return weekDates;
	}

	@Override
	public void save(List<ScheduleEventDTO> dtos) {
		for (ScheduleEventDTO dto : dtos) {
			ScheduleEvent entity = createEntity(dto);
			if (validateEvent(entity)) {
				scheduleEntryDAO.update(entity);
			} else {
				Log.e(this.getClass().getCanonicalName(), "Invalid entity not persisted");
			}
		}
	}

	private ScheduleEvent createEntity(ScheduleEventDTO dto) {
		ScheduleEvent entity = new ScheduleEvent();
		entity.setActive(dto.isActive());
		entity.setId(dto.getId());
		entity.setInterval(dto.getInterval());
		entity.setStartDate(dto.getStartDate());
		entity.setStopDate(dto.getStopDate());
		entity.setType(dto.getType());
		
		return entity;
	}
	
	private boolean validateEvent(ScheduleEvent event) {
		Calendar start = Calendar.getInstance();
		Calendar stop = Calendar.getInstance();
		start.setTime(event.getStartDate());
		stop.setTime(event.getStopDate());
		if (start.compareTo(stop) > 0) {
			return false;
		}
		if (start.get(Calendar.YEAR) != stop.get(Calendar.YEAR)) {
			return false;
		}
		if (start.get(Calendar.MONTH) != stop.get(Calendar.MONTH)) {
			return false;
		}
		if (start.get(Calendar.DATE) != stop.get(Calendar.DATE)) {
			return false;
		}
		
		return true;
	}
}
