package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainDisplay implements MainActivity.Display {
	
	private Activity activity;
	
	private Button startBtn;
	private Button stopBtn;
	private View loaderSpinner;
	private AlertDialog popup;
	private TextView bwfRight;
	private TextView bwfLeft;
	private TextView rangeRight;
	private TextView rangeLeft;
	private TextView currentState;

	public MainDisplay(Activity activity) {
		this.activity = activity;
		
		startBtn = (Button) activity.findViewById(R.id.startFSM);
		stopBtn = (Button) activity.findViewById(R.id.stopFSM);
		loaderSpinner = (View) activity.findViewById(R.id.spinner);
		bwfLeft = (TextView) activity.findViewById(R.id.bwfLeft);
		bwfRight = (TextView) activity.findViewById(R.id.bwfRight);
		rangeLeft = (TextView) activity.findViewById(R.id.rangeLeft);
		rangeRight = (TextView) activity.findViewById(R.id.rangeRight);
		currentState = (TextView) activity.findViewById(R.id.currentState);
	}

	private AlertDialog createPopup(Activity activity, final Runnable runnable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Gräsklipparen är inte ansluten");
		builder.setCancelable(false);
		builder.setNeutralButton("Stäng", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				runnable.run();
			}
		});
		return builder.create();
	}

	@Override
	public Button getStartBtn() {
		return startBtn;
	}

	@Override
	public Button getStopBtn() {
		return stopBtn;
	}

	@Override
	public View getLoaderSpinner() {
		return loaderSpinner;
	}

	@Override
	public void showNotConnectedPopup(Runnable onCancel) {
		if (popup != null && popup.isShowing()) {
			hideNotConnectedPopup();
		}
		popup = createPopup(activity, onCancel);
		popup.show();
	}

	@Override
	public void hideNotConnectedPopup() {
		if (popup != null) {
			popup.hide();
		}
	}

	@Override
	public void setBwfLeft(String value) {
		bwfLeft.setText(value);
	}

	@Override
	public void setBwfRight(String value) {
		bwfRight.setText(value);
	}

	@Override
	public void setRangeLeft(String value) {
		rangeLeft.setText(value);
	}

	@Override
	public void setRangeRight(String value) {
		rangeRight.setText(value);
	}

	@Override
	public void setCurrentState(String state) {
		currentState.setText(state);
	}
}
