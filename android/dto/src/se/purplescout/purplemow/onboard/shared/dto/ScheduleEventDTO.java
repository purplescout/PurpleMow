package se.purplescout.purplemow.onboard.shared.dto;

import java.util.Date;

import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.RecurringInterval;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.Type;

public class ScheduleEventDTO {

	private int id;
	private Type type;
	private Date startDate;
	private Date endDate;
	private RecurringInterval interval;
	private boolean fromDB;
	private boolean changed;

	public ScheduleEventDTO() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setStopDate(Date endDate) {
		this.endDate = endDate;
	}

	public RecurringInterval getInterval() {
		return interval;
	}

	public void setInterval(RecurringInterval interval) {
		this.interval = interval;
	}

	public boolean isFromDB() {
		return fromDB;
	}

	public void setFromDB(boolean fromDB) {
		this.fromDB = fromDB;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
