package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

public class MainDisplay implements MainActivity.Display {

	private Button startBtn;
	private Button stopBtn;
	private View logView;
	private View loaderSpinner;
	private AlertDialog popup;

	public MainDisplay(Activity activity) {
		startBtn = ((Button) activity.findViewById(R.id.startFSM));
		stopBtn = ((Button) activity.findViewById(R.id.stopFSM));
		logView = ((View) activity.findViewById(R.id.log));
		loaderSpinner = ((View) activity.findViewById(R.id.spinner));

		createPopup(activity);
	}

	private void createPopup(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Gräsklipparen är inte ansluten");
		builder.setCancelable(false);
		builder.setNeutralButton("Stäng", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		popup = builder.create();
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
	public View getLogView() {
		return logView;
	}

	@Override
	public View getLoaderSpinner() {
		return loaderSpinner;
	}

	@Override
	public void showNotConnectedPopup() {
		popup.show();
	}

	@Override
	public void hideNotConnectedPopup() {
		popup.hide();
	}
}
