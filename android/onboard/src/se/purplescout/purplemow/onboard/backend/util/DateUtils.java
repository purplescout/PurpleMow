package se.purplescout.purplemow.onboard.backend.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils {

	public static final int[] weekDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.TUESDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

	public static class OperableDate extends Date {

		private static final long serialVersionUID = 3695355191206474291L;

		public OperableDate(long milliseconds) {
			super(milliseconds);
		}

		public static OperableDate wrap(Date date) {
			return new OperableDate(date.getTime());
		}

		public boolean greaterThan(Date date) {
			return getTime() > date.getTime();
		}

		public boolean greaterThanOrEqualTo(Date date) {
			return getTime() >= date.getTime();
		}

		public boolean lesserThanOrEqualTo(Date date) {
			return getTime() <= date.getTime();
		}

		public boolean lesserThan(Date date) {
			return getTime() < date.getTime();
		}
	}

	public static List<Date> getWeek(Date date) {
		List<Date> result = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		for (int day : weekDays) {
			cal.set(Calendar.DAY_OF_WEEK, day);
			result.add(cal.getTime());
		}

		return result;
	}
}
