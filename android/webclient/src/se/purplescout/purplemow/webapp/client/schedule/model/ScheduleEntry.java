package se.purplescout.purplemow.webapp.client.schedule.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.Type;
import se.purplescout.purplemow.onboard.shared.dto.ScheduleEventDTO;
import se.purplescout.purplemow.webapp.client.schedule.presenter.SchedulePresenter.ViewInterval;

public class ScheduleEntry {
	private final int column;
	private final int startRow;
	private final int endRow;
	private final Type type;

	private ScheduleEntry(int column, int startRow, int endRow, Type type) {
		this.column = column;
		this.startRow = startRow;
		this.endRow = endRow;
		this.type = type;
	}

	public int getColumn() {
		return column;
	}

	public int getStartRow() {
		return startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public Type getType() {
		return type;
	}

	public static class Builder {
		ScheduleEventDTO scheduleEventDTO;
		ViewInterval interval = ViewInterval.WEEKLY;

		public Builder(ScheduleEventDTO dto) {
			this.scheduleEventDTO = dto;
		}

		public Builder setInterval(ViewInterval interval) {
			this.interval = interval;
			return this;
		}

		public List<ScheduleEntry> build() {
			List<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
			if (interval == ViewInterval.WEEKLY) {
				entries.addAll(new WeeklyEntriesBuilder(scheduleEventDTO).build());
			}

			return entries;
		}
	}

	static class WeeklyEntriesBuilder {
		ScheduleEventDTO scheduleEventDTO;

		public WeeklyEntriesBuilder(ScheduleEventDTO scheduleEventDTO) {
			this.scheduleEventDTO = scheduleEventDTO;
		}

		public List<ScheduleEntry> build() {
			List<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
			int startRow = calulateRow(scheduleEventDTO.getStartDate());
			int endRow = calulateRow(scheduleEventDTO.getEndDate()) - 1;

			ScheduleEntry entry;
			switch (scheduleEventDTO.getInterval()) {
			case DAILY:
				for (int day = 0; day < 7; day++) {
					entry = new ScheduleEntry(day, startRow, endRow, scheduleEventDTO.getType());
					entries.add(entry);
				}
				break;
			case WEEKLY:
				entry = new ScheduleEntry(calculateColumn(scheduleEventDTO.getStartDate()), startRow, endRow, scheduleEventDTO.getType());
				entries.add(entry);
				break;
			case MONTHLY:
				entry = new ScheduleEntry(calculateColumn(scheduleEventDTO.getStartDate()), startRow, endRow, scheduleEventDTO.getType());
				entries.add(entry);
				break;
			case ALL_WEEKDAYS:
				for (int day = 0; day < 5; day++) {
					entry = new ScheduleEntry(day, startRow, endRow, scheduleEventDTO.getType());
					entries.add(entry);
				}
				break;
			case ALL_WEEKENDS:
				for (int day = 5; day < 7; day++) {
					entry = new ScheduleEntry(day, startRow, endRow, scheduleEventDTO.getType());
					entries.add(entry);
				}
				break;
			}

			return entries;
		}

		@SuppressWarnings("deprecation")
		private int calulateRow(Date endDate) {
			int row = endDate.getHours() * 2;
			if (endDate.getMinutes() > 29) {
				row++;
			}

			return row;
		}

		@SuppressWarnings("deprecation")
		private int calculateColumn(Date date) {
			if (date.getDay() == 0) {
				return 6;
			}

			return date.getDay() - 1;
		}
	}
}
