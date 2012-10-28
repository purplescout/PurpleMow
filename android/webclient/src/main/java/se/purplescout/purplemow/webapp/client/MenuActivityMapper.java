package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.menu.presenter.MenuPresenter;
import se.purplescout.purplemow.webapp.client.menu.view.MenuView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class MenuActivityMapper implements ActivityMapper {

	private PurpleMowPlaceHistoryMapper historyMapper;

	public MenuActivityMapper(PurpleMowPlaceHistoryMapper historyMapper) {
		this.historyMapper = historyMapper;
	}

	@Override
	public Activity getActivity(Place place) {
		MenuView view = new MenuView();
		return new MenuPresenter(view, place, historyMapper);
	}
}