package se.purplescout.purplemow.webapp.client.home.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class HomePlace extends Place {

	private String token;

	public HomePlace() {
		this("");
	}

	public HomePlace(String token) {
		this.token = token;
	}
	
	@Prefix("home")
	public static class Tokenizer implements PlaceTokenizer<HomePlace> {

		@Override
		public HomePlace getPlace(String token) {
			return new HomePlace(token);
		}

		@Override
		public String getToken(HomePlace place) {
			return place.token;
		}
	}
}
