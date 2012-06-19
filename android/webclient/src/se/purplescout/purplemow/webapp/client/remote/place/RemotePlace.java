package se.purplescout.purplemow.webapp.client.remote.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class RemotePlace extends Place {

	private String token;

	public RemotePlace() {
		this("");
	}

	public RemotePlace(String token) {
		this.token = token;
	}

	@Prefix("remote")
	public static class Tokenizer implements PlaceTokenizer<RemotePlace> {

		@Override
		public RemotePlace getPlace(String token) {
			return new RemotePlace(token);
		}

		@Override
		public String getToken(RemotePlace place) {
			return place.token;
		}
	}
}
