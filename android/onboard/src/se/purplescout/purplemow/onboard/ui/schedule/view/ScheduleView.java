package se.purplescout.purplemow.onboard.ui.schedule.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.ActivityBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.schedule.activity.ScheduleActivity;
import android.app.Activity;
import android.widget.ListView;

@ContentView(R.layout.activity_schedule)
public class ScheduleView extends ActivityBinderView implements ScheduleActivity.ViewDisplay {

	@UiField(R.id.scheduleListView) ListView listView;
	
	public ScheduleView(Activity activity) {
		super(activity);
	}

	@Override
	public ListView getListView() {
		return listView;
	}
}
