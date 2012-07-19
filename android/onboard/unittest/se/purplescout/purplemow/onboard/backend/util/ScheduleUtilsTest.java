package se.purplescout.purplemow.onboard.backend.util;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.RecurringInterval;

public class ScheduleUtilsTest extends TestCase {
	static {

	}
	public void testGetFirstOccurrenceNoInterval() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);

		cal.set(2012, Calendar.JULY, 1, 1, 0, 5);
		Date d20120701_010005 = cal.getTime();

		cal.set(2012, Calendar.JULY, 21, 16, 8, 5);
		Date d20120721_160805 = cal.getTime();

		cal.set(2012, Calendar.JULY, 21, 16, 9, 39);
		Date d20120721_160939 = cal.getTime();

		ScheduleEvent event = new ScheduleEvent();
		event.setActive(true);
		event.setInterval(null);
		event.setStartDate(d20120721_160805);

		Date actualDate = ScheduleUtils.getFirstOccurrence(event, d20120701_010005);
		Assert.assertEquals(actualDate, d20120721_160805);

		actualDate = ScheduleUtils.getFirstOccurrence(event, d20120721_160939);
		Assert.assertNull(actualDate);

	}
	public void testGetFirstOccurrenceDaily() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);

		cal.set(2012, Calendar.JULY, 1, 1, 0, 5);
		Date d20120701_010005 = cal.getTime();

		cal.set(2012, Calendar.JULY, 21, 16, 8, 5);
		Date d20120721_160805 = cal.getTime();

		cal.set(2012, Calendar.JULY, 21, 16, 9, 39);
		Date d20120721_160939 = cal.getTime();

		cal.set(2012, Calendar.JULY, 22, 16, 8, 5);
		Date d20120722_160805 = cal.getTime();

		cal.set(2012, Calendar.AUGUST, 7, 23, 2, 57);
		Date d20120807_230257 = cal.getTime();

		cal.set(2012, Calendar.AUGUST, 8, 16, 8, 5);
		Date d20120808_160805 = cal.getTime();

		cal.set(2015, Calendar.AUGUST, 7, 23, 2, 57);
		Date d20150807_230257 = cal.getTime();

		cal.set(2015, Calendar.AUGUST, 8, 16, 8, 5);
		Date d20150808_160805 = cal.getTime();

		ScheduleEvent event = new ScheduleEvent();
		event.setActive(true);
		event.setInterval(null);
		event.setStartDate(d20120721_160805);

		event.setInterval(RecurringInterval.DAILY);
		Date actualDate = ScheduleUtils.getFirstOccurrence(event, d20120721_160939);
		Assert.assertEquals(d20120722_160805, actualDate);

		actualDate = ScheduleUtils.getFirstOccurrence(event, d20120701_010005);
		Assert.assertEquals(d20120721_160805, actualDate);

		actualDate = ScheduleUtils.getFirstOccurrence(event, d20120807_230257);
		Assert.assertEquals(d20120808_160805, actualDate);

		actualDate = ScheduleUtils.getFirstOccurrence(event, d20150807_230257);
		Assert.assertEquals(d20150808_160805, actualDate);
	}
}
