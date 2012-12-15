package se.purplescout.purplemow.onboard.backend.service.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;
import se.purplescout.purplemow.onboard.shared.schedule.dto.RecurringInterval;
import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import android.util.Log;

@Singleton
public class ScheduleServiceImpl implements ScheduleService {

	private final ScheduleEventDAO scheduleEntryDAO;
	private final ScheduledExecutorService scheduler;
	private final CoreBus coreBus = CoreBus.getInstance();

	private final List<ScheduledFuture<?>> scheduledEvents = new ArrayList<ScheduledFuture<?>>();

	@Inject
	public ScheduleServiceImpl(ScheduleEventDAO scheduleEntryDAO, ScheduledExecutorService scheduler) {
		this.scheduleEntryDAO = scheduleEntryDAO;
		this.scheduler = scheduler;
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
		boolean changed = false;
		for (ScheduleEventDTO dto : dtos) {
			ScheduleEvent entity = createEntity(dto);
			if (validateEvent(entity)) {
				scheduleEntryDAO.update(entity);
				changed = true;
			} else {
				Log.e(this.getClass().getSimpleName(), "Invalid entity not persisted");
			}
		}
		if (changed) {
			initScheduler();
		}
	}

	@Override
	public void save(ScheduleEventDTO scheduleEvent) {
		boolean changed = false;
		if (scheduleEvent.isChanged()) {
			ScheduleEvent entity = createEntity(scheduleEvent);
			if (validateEvent(entity)) {
				scheduleEntryDAO.update(entity);
				changed = true;
			} else {
				Log.e(this.getClass().getSimpleName(), "Invalid entity not persisted");
			}
		}
		if (changed) {
			initScheduler();
		}
	}

	@Override
	public void initScheduler() {
		clearScheduler();
		List<ScheduleEvent> events = scheduleEntryDAO.listAll();

		for (ScheduleEvent event : events) {
			if (event.getInterval() == null || event.getInterval() != RecurringInterval.WEEKLY) {
				Log.w(this.getClass().getSimpleName(), "Only weekly events are supported");
				continue;
			}
			if (!event.isActive()) {
				continue;
			}
			scheduleNextStartMowEvent(event);
			scheduleNextStopMowEvent(event);
		}
	}

	private void clearScheduler() {
		Iterator<ScheduledFuture<?>> it = scheduledEvents.iterator();
		while (it.hasNext()) {
			ScheduledFuture<?> event = it.next();
			event.cancel(true);
			it.remove();
		}
	}

	private long getTimeUntilNextStart(ScheduleEvent event) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(event.getStartDate());

		cal.set(Calendar.DAY_OF_WEEK, startCal.get(Calendar.DAY_OF_WEEK));
		cal.set(Calendar.HOUR, startCal.get(Calendar.HOUR));
		cal.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		long val = cal.getTime().getTime() - new Date().getTime();
		if (val < 0) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			val = cal.getTime().getTime() - new Date().getTime();
		}

		Log.i(this.getClass().getSimpleName(), "Start will occur in " + val + " millis");
		return cal.getTime().getTime() - new Date().getTime();
	}

	private long getTimeUntilNextStop(ScheduleEvent event) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(event.getStopDate());

		cal.set(Calendar.DAY_OF_WEEK, startCal.get(Calendar.DAY_OF_WEEK));
		cal.set(Calendar.HOUR, startCal.get(Calendar.HOUR));
		cal.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		long val = cal.getTime().getTime() - new Date().getTime();
		if (val < 0) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			val = cal.getTime().getTime() - new Date().getTime();
		}

		Log.i(this.getClass().getSimpleName(), "Stop will occur in " + val + " millis");
		return val;
	}

	private void scheduleNextStopMowEvent(final ScheduleEvent event) {
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				Log.i(ScheduleService.class.getSimpleName(), "Stop event");
				coreBus.fireEvent(new StopEvent());
				scheduleNextStopMowEvent(event);
			}
		}, getTimeUntilNextStop(event), TimeUnit.MILLISECONDS);
	}

	private void scheduleNextStartMowEvent(final ScheduleEvent event) {
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				Log.i(ScheduleService.class.getSimpleName(), "Start event");
				coreBus.fireEvent(new MoveEvent(Constants.FULL_SPEED, Direction.FORWARD));
				scheduleNextStartMowEvent(event);
			}
		}, getTimeUntilNextStart(event), TimeUnit.MILLISECONDS);
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
