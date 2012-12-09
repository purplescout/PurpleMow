package se.purplescout.purplemow.onboard.ui.schedule.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.InflaterBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.schedule.activity.ScheduleActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

@ContentView(R.layout.list_schedule)
public class ScheduleListView extends InflaterBinderView implements ScheduleActivity.ListViewDisplay {

	@UiField(R.id.scheduleDayText) TextView dayText;
	@UiField(R.id.scheduleFromBtn) Button fromBtn;
	@UiField(R.id.scheduleToBtn) Button toBtn;
	@UiField(R.id.scheduleCheckBox) CheckBox checkBox;

	public ScheduleListView(LayoutInflater layoutInflater, View parentView) {
		super(layoutInflater, parentView);
	}

	@Override
	public TextView getDayText() {
		return dayText;
	}

	@Override
	public Button getFromBtn() {
		return fromBtn;
	}

	@Override
	public Button getToBtn() {
		return toBtn;
	}

	@Override
	public CheckBox getCheckbox() {
		return checkBox;
	}

	@Override
	public View getView() {
		return parentView;
	}
}
