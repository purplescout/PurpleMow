package se.purplescout.purplemow.onboard.web.service.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.backend.dao.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.util.DateUtils;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;
import se.purplescout.purplemow.onboard.shared.dto.ScheduleEventDTO;
import se.purplescout.purplemow.onboard.web.service.ScheduleService;

public class ScheduleServiceImpl implements ScheduleService {

	private ScheduleEventDAO scheduleEntryDAO;

	public ScheduleServiceImpl(ScheduleEventDAO scheduleEntryDAO) {
		this.scheduleEntryDAO = scheduleEntryDAO;
	}

	@Override
	public List<Date> getDatesForWeek(Date date) {
		return DateUtils.getWeek(date);
	}

	@Override
	public List<ScheduleEventDTO> getScheduleForWeek(Date date) {
		return createDTOs(scheduleEntryDAO.listAll());
	}

	private ScheduleEvent createEntity(ScheduleEventDTO dto) {
		ScheduleEvent entity = new  ScheduleEvent();
		entity.setId(dto.getId());
		entity.setActive(true);
		entity.setInterval(dto.getInterval());
		entity.setStartDate(dto.getStartDate());
		entity.setType(dto.getType());

		return entity;
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

		return dto;
	}
}
