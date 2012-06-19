package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.remote.place.RemotePlace;
import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter;
import se.purplescout.purplemow.webapp.client.remote.service.RemoteService;
import se.purplescout.purplemow.webapp.client.remote.service.RemoteServiceProxy;
import se.purplescout.purplemow.webapp.client.remote.view.RemoteView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;

public class CenterActivityMapper implements ActivityMapper {

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof RemotePlace) {
			return new RemotePresenter(new RemoteView(), GWT.<RemoteService>create(RemoteServiceProxy.class));
		}
		
		return new AbstractActivity() {

			@Override
			public void start(AcceptsOneWidget panel, EventBus eventBus) {
				panel.setWidget(new Label("Hello World"));
			}
		};
	}
}
