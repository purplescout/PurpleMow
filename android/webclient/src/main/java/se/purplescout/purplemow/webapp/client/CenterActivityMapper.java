package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.log.place.LogPlace;
import se.purplescout.purplemow.webapp.client.log.presenter.LogcatPresenter;
import se.purplescout.purplemow.webapp.client.log.service.LogService;
import se.purplescout.purplemow.webapp.client.log.view.LogcatView;
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

public class CenterActivityMapper implements ActivityMapper {

	public CenterActivityMapper() {
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof RemotePlace) {
			return new RemotePresenter(new RemoteView(), GWT.<RemoteService>create(RemoteService.class));
		}
		if (place instanceof SchedulePlace){
			return new SchedulePresenter(new ScheduleView(), GWT.<ScheduleService>create(ScheduleService.class));
		}
		if (place instanceof LogPlace) {
			return new LogcatPresenter(new LogcatView(), GWT.<LogService>create(LogService.class));
		}
		
		return null;
	}
}
