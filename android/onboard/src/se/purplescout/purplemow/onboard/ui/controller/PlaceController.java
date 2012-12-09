package se.purplescout.purplemow.onboard.ui.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class PlaceController {

	public static void goTo(Context context, Place place) {
		Class<? extends Activity> activityClass = ActivityMapper.getActivity(place);
		Intent intent = new Intent(context, activityClass);
		context.startActivity(intent);
	}
}
