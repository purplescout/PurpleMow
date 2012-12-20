package se.purplescout.purplemow.onboard.ui.controller;

import se.purplescout.purplemow.onboard.ui.configure.activity.ConfigureActivity;
import se.purplescout.purplemow.onboard.ui.configure.place.ConfigurePlace;
import se.purplescout.purplemow.onboard.ui.home.activity.HomeActivity;
import se.purplescout.purplemow.onboard.ui.remote.activity.RemoteActivity;
import se.purplescout.purplemow.onboard.ui.remote.place.RemotePlace;
import se.purplescout.purplemow.onboard.ui.schedule.activity.ScheduleActivity;
import se.purplescout.purplemow.onboard.ui.schedule.place.SchedulePlace;
import se.purplescout.purplemow.onboard.ui.sensors.activity.SensorsActivity;
import se.purplescout.purplemow.onboard.ui.sensors.place.SensorsPlace;
import android.app.Activity;

public class ActivityMapper {

	public static Class<? extends Activity> getActivity(Place place) {
		if (place instanceof SensorsPlace) {
			return SensorsActivity.class;
		}
		if (place instanceof RemotePlace) {
			return RemoteActivity.class;
		}
		if (place instanceof SchedulePlace) {
			return ScheduleActivity.class;
		}
		if (place instanceof ConfigurePlace) {
			return ConfigureActivity.class;
		}

		return HomeActivity.class;
	}
}
