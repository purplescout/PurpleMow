package se.purplescout.purplemow.onboard.backend.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import se.purplescout.purplemow.onboard.backend.util.DateUtils.OperableDate;

public class DateUtilsTest extends TestCase {

	public void testOperableDate() {
		Date d1 = new Date(1);
		Date d2 = new Date(2);
		Assert.assertTrue(OperableDate.wrap(d1).lesserThan(d2));
		Assert.assertTrue(OperableDate.wrap(d1).lesserThanOrEqualTo(d2));
		Assert.assertFalse(OperableDate.wrap(d1).greaterThanOrEqualTo(d2));
		Assert.assertFalse(OperableDate.wrap(d1).greaterThan(d2));

		d1 = new Date(2);
		d2 = new Date(1);
		Assert.assertFalse(OperableDate.wrap(d1).lesserThan(d2));
		Assert.assertFalse(OperableDate.wrap(d1).lesserThanOrEqualTo(d2));
		Assert.assertTrue(OperableDate.wrap(d1).greaterThanOrEqualTo(d2));
		Assert.assertTrue(OperableDate.wrap(d1).greaterThan(d2));

		d1 = new Date(1);
		Assert.assertFalse(OperableDate.wrap(d1).lesserThan(d2));
		Assert.assertTrue(OperableDate.wrap(d1).lesserThanOrEqualTo(d2));
		Assert.assertTrue(OperableDate.wrap(d1).greaterThanOrEqualTo(d2));
		Assert.assertFalse(OperableDate.wrap(d1).greaterThan(d2));
	}

	public void testGetWeek() {
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 6, 22, 12, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date refDate = cal.getTime();

		List<Date> weekDays = DateUtils.getWeek(refDate);
		Assert.assertEquals(7, weekDays.size());

		cal.setTime(weekDays.get(0));
		Assert.assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
		cal.setTime(weekDays.get(1));
		Assert.assertEquals(Calendar.TUESDAY, cal.get(Calendar.DAY_OF_WEEK));
		cal.setTime(weekDays.get(4));
		Assert.assertEquals(Calendar.FRIDAY, cal.get(Calendar.DAY_OF_WEEK));
		cal.setTime(weekDays.get(6));
		Assert.assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));

		Assert.assertEquals(refDate, cal.getTime());
	}
}
