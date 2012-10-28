package se.purplescout.purplemow.webapp.client.log.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class LogPlace extends Place {

	@Prefix("log")
	public static class Tokenizer implements PlaceTokenizer<LogPlace> {

		@Override
		public LogPlace getPlace(String token) {
			return new LogPlace();
		}

		@Override
		public String getToken(LogPlace place) {
			return "";
		}
	}
}
