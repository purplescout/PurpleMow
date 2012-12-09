package se.purplescout.purplemow.onboard.ui.sensors.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.sensors.activity.SensorsActivity;
import android.app.Activity;
import android.widget.TextView;

public class SensorsView implements SensorsActivity.ViewDisplay {
	
	private TextView bwf;
	private TextView rangeRight;
	private TextView rangeLeft;
	private TextView currentState;

	public SensorsView(Activity activity) {	
		bwf = (TextView) activity.findViewById(R.id.bwf);
		rangeLeft = (TextView) activity.findViewById(R.id.rangeLeft);
		rangeRight = (TextView) activity.findViewById(R.id.rangeRight);
		currentState = (TextView) activity.findViewById(R.id.currentState);
	}
	
	@Override
	public void setBwfValue(String value) {
		bwf.setText(value);
	}

	@Override
	public void setLeftRangeValue(String value) {
		rangeLeft.setText(value);
	}

	@Override
	public void setRightRangeValue(String value) {
		rangeRight.setText(value);
	}

	@Override
	public void setCurrentState(String state) {
		currentState.setText(state);
	}
}
