package se.purplescout.purplemow.onboard.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import se.purplescout.purplemow.onboard.shared.schedule.dto.RecurringInterval;
import se.purplescout.purplemow.onboard.shared.schedule.dto.Type;

@Entity(name = "schedule_event")
public class ScheduleEvent {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name = "start_date", nullable = false)
	private Date startDate;

	@Column(name = "stop_date", nullable = false)
	private Date stopDate;

	@Column(name = "interval")
	@Enumerated(EnumType.STRING)
	private RecurringInterval interval;

	@Column(name = "is_active")
	private boolean isActive;

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

	public Date getStopDate() {
		return stopDate;
	}

	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	public RecurringInterval getInterval() {
		return interval;
	}

	public void setInterval(RecurringInterval interval) {
		this.interval = interval;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
