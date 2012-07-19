package se.purplescout.purplemow.onboard.backend.util;

import java.util.Calendar;
import java.util.Date;

import se.purplescout.purplemow.onboard.backend.util.DateUtils.OperableDate;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;

public class ScheduleUtils {

	private ScheduleUtils() {
	}

	/**
	 * Returns the first occurrence of a {@link ScheduleEvent} from a given {@link Date}
	 *
	 * @param event
	 * @param startFrom
	 * @return
	 */
	public static Date getFirstOccurrence(ScheduleEvent event, Date startFrom) {
		if (event.getInterval() == null && OperableDate.wrap(event.getStartDate()).lesserThan(startFrom)) {
			return null;
		}

		if (OperableDate.wrap(event.getStartDate()).greaterThanOrEqualTo(startFrom)) {
			return event.getStartDate();
		}

		Calendar startFromCal = Calendar.getInstance();
		startFromCal.setTime(startFrom);
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.setTime(event.getStartDate());

		switch (event.getInterval()) {
		case DAILY:
			startDateCal.set(startFromCal.get(Calendar.YEAR), startFromCal.get(Calendar.MONTH), startFromCal.get(Calendar.DATE));
			Date normalizedStartDate = startDateCal.getTime();
			if (OperableDate.wrap(normalizedStartDate).lesserThan(startFrom)) {
				startDateCal.add(Calendar.DATE, 1);
			}

			return startDateCal.getTime();
		default:
			break;
		}
		return null;
	}
}
