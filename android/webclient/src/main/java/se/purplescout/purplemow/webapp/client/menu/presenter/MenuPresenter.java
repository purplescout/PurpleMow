package se.purplescout.purplemow.webapp.client.menu.presenter;

import se.purplescout.purplemow.webapp.client.home.place.HomePlace;
import se.purplescout.purplemow.webapp.client.log.place.LogPlace;
import se.purplescout.purplemow.webapp.client.remote.place.RemotePlace;
import se.purplescout.purplemow.webapp.client.schedule.place.SchedulePlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;

public class MenuPresenter extends AbstractActivity {
	public interface View extends IsWidget {

		void addMenu(String name, String histotyToken, boolean isActive);
	}

	View view;
	Place place;
	PlaceHistoryMapper historyMapper;

	public MenuPresenter(View view, Place place, PlaceHistoryMapper historyMapper) {
		this.view = view;
		this.place = place;
		this.historyMapper = historyMapper;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view.asWidget());
		bind();
	}

	private void bind() {
		view.addMenu("Home", historyMapper.getToken(new HomePlace()), (place instanceof HomePlace));
		view.addMenu("Remote", historyMapper.getToken(new RemotePlace()), (place instanceof RemotePlace));
		view.addMenu("Schedule", historyMapper.getToken(new SchedulePlace()), (place instanceof SchedulePlace));
		view.addMenu("Log", historyMapper.getToken(new LogPlace()), (place instanceof LogPlace));
	}
}
