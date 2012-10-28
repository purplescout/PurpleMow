package se.purplescout.purplemow.webapp.client.schedule.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SchedulePlace extends Place {

	private String token;

	public SchedulePlace() {
		this("");
	}

	public SchedulePlace(String token) {
		this.token = token;
	}

	@Prefix("schedule")
	public static class Tokenizer implements PlaceTokenizer<SchedulePlace> {

		@Override
		public SchedulePlace getPlace(String token) {
			return new SchedulePlace(token);
		}

		@Override
		public String getToken(SchedulePlace place) {
			return place.token;
		}
	}
}