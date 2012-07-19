package se.purplescout.purplemow.webapp.test.client.schedule.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.RecurringInterval;
import se.purplescout.purplemow.onboard.db.entity.ScheduleEvent.Type;
import se.purplescout.purplemow.onboard.shared.dto.ScheduleEventDTO;
import se.purplescout.purplemow.webapp.client.schedule.model.ScheduleEntry;
import se.purplescout.purplemow.webapp.client.schedule.presenter.SchedulePresenter.ViewInterval;


public class ScheduleEntryTest extends TestCase {

	public void testBuilderWeeklyLayout() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);

		cal.set(2012, Calendar.JULY, 1, 10, 0, 0);
		Date d20120701_100000 = cal.getTime();

		cal.set(2012, Calendar.JULY, 1, 12, 30, 0);
		Date d20120701_123000 = cal.getTime();

		ScheduleEventDTO dto = new ScheduleEventDTO();
		dto.setStartDate(d20120701_100000);
		dto.setStopDate(d20120701_123000);
		dto.setInterval(RecurringInterval.MONTHLY);
		dto.setType(Type.MOWING);

		List<ScheduleEntry> entries = new ScheduleEntry.Builder(dto).setInterval(ViewInterval.WEEKLY).build();
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(6, entries.get(0).getColumn());
		Assert.assertEquals(20, entries.get(0).getStartRow());
		Assert.assertEquals(24, entries.get(0).getEndRow());
		Assert.assertEquals(Type.MOWING, entries.get(0).getType());

		dto.setInterval(RecurringInterval.WEEKLY);
		entries = new ScheduleEntry.Builder(dto).setInterval(ViewInterval.WEEKLY).build();
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(6, entries.get(0).getColumn());
		Assert.assertEquals(20, entries.get(0).getStartRow());
		Assert.assertEquals(24, entries.get(0).getEndRow());
		Assert.assertEquals(Type.MOWING, entries.get(0).getType());

		dto.setInterval(RecurringInterval.DAILY);
		entries = new ScheduleEntry.Builder(dto).setInterval(ViewInterval.WEEKLY).build();
		Assert.assertNotNull(entries);
		Assert.assertEquals(7, entries.size());
		Assert.assertEquals(4, entries.get(4).getColumn());
		Assert.assertEquals(20, entries.get(0).getStartRow());
		Assert.assertEquals(24, entries.get(3).getEndRow());
		Assert.assertEquals(Type.MOWING, entries.get(4).getType());

		dto.setInterval(RecurringInterval.ALL_WEEKDAYS);
		entries = new ScheduleEntry.Builder(dto).setInterval(ViewInterval.WEEKLY).build();
		Assert.assertNotNull(entries);
		Assert.assertEquals(5, entries.size());
		Assert.assertEquals(4, entries.get(4).getColumn());
		Assert.assertEquals(20, entries.get(0).getStartRow());
		Assert.assertEquals(24, entries.get(3).getEndRow());
		Assert.assertEquals(Type.MOWING, entries.get(0).getType());

		dto.setInterval(RecurringInterval.ALL_WEEKENDS);
		entries = new ScheduleEntry.Builder(dto).setInterval(ViewInterval.WEEKLY).build();
		Assert.assertNotNull(entries);
		Assert.assertEquals(2, entries.size());
		Assert.assertEquals(5, entries.get(0).getColumn());
		Assert.assertEquals(6, entries.get(1).getColumn());
		Assert.assertEquals(20, entries.get(0).getStartRow());
		Assert.assertEquals(24, entries.get(1).getEndRow());
		Assert.assertEquals(Type.MOWING, entries.get(0).getType());
	}
}
