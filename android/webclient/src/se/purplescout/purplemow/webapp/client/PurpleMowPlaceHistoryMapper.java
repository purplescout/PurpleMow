package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.home.place.HomePlace;
import se.purplescout.purplemow.webapp.client.log.place.LogPlace;
import se.purplescout.purplemow.webapp.client.remote.place.RemotePlace;
import se.purplescout.purplemow.webapp.client.schedule.place.SchedulePlace;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({
	HomePlace.Tokenizer.class,
	RemotePlace.Tokenizer.class,
	SchedulePlace.Tokenizer.class,
	LogPlace.Tokenizer.class})
public interface PurpleMowPlaceHistoryMapper extends PlaceHistoryMapper {
}
