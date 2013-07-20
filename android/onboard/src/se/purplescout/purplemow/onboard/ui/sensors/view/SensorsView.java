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
	private TextView bumperCounter;
	private TextView outsideBWF;
	private TextView batteryLevel;
	private TextView messageArea;
	private Integer bumperCount = 0;
	
	public SensorsView(Activity activity) {
		bwf = (TextView) activity.findViewById(R.id.bwf);
		rangeLeft = (TextView) activity.findViewById(R.id.rangeLeft);
		rangeRight = (TextView) activity.findViewById(R.id.rangeRight);
		currentState = (TextView) activity.findViewById(R.id.currentState);
		bumperCounter = (TextView) activity.findViewById(R.id.bumperHits);
		batteryLevel = (TextView) activity.findViewById(R.id.batteryLevel);
		outsideBWF = (TextView) activity.findViewById(R.id.outsideBWF);
		messageArea = (TextView) activity.findViewById(R.id.messageArea);
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

	@Override
	public void increaseBumperCounter() {
		bumperCount ++;
		bumperCounter.setText(Integer.toString(bumperCount));
		
	}

	@Override
	public void setOutsideBWF(String value) {
		outsideBWF.setText(value);
	}


	@Override
	public void setBatteryLevel(int value) {
		batteryLevel.setText(Integer.toString(value));
	}

	@Override
	public void setMessage(String message) {
		messageArea.setText(message);
	}
}
