package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.remote.place.RemotePlace;
import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter;
import se.purplescout.purplemow.webapp.client.remote.service.RemoteService;
import se.purplescout.purplemow.webapp.client.remote.view.RemoteView;
import se.purplescout.purplemow.webapp.client.schedule.place.SchedulePlace;
import se.purplescout.purplemow.webapp.client.schedule.presenter.SchedulePresenter;
import se.purplescout.purplemow.webapp.client.schedule.service.ScheduleService;
import se.purplescout.purplemow.webapp.client.schedule.view.ScheduleView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class CenterActivityMapper implements ActivityMapper {

	private SimpleEventBus eventBus;

	public CenterActivityMapper(SimpleEventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof RemotePlace) {
			return new RemotePresenter(new RemoteView(), GWT.<RemoteService>create(RemoteService.class));
		} else if (place instanceof SchedulePlace){
			return new SchedulePresenter(new ScheduleView(), GWT.<ScheduleService>create(ScheduleService.class), eventBus);
		}
		
		return null;
	}
}
