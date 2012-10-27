package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.log.place.LogPlace;
import se.purplescout.purplemow.webapp.client.log.presenter.LogPresenter;
import se.purplescout.purplemow.webapp.client.log.view.LogView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class TopActivityMapper implements ActivityMapper {

	public TopActivityMapper() {
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof LogPlace) {
			return new LogPresenter(new LogView());
		}

		return null;
	}
}
