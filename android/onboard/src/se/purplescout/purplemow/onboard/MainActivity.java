package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import se.purplescout.purplemow.onboard.service.FSMService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	public interface Display {
		
		Button getStartBtn();
		
		Button getStopBtn();
		
		View getLogView();
		
		View getLoaderSpinner();
	}
	
	Display display;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("PurpleMow", "¤¤¤¤¤¤Nu startar det!¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤");
		
		display = new MainDisplay(this);
		bind();	
	}

	private void bind() {
		display.getStartBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startFSM();
			}
		});

		display.getStopBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopFSM();
			}
		});
	}

	private void startFSM() {
		display.getLogView().setVisibility(View.GONE);
		display.getLoaderSpinner().setVisibility(View.VISIBLE);
		display.getStartBtn().setEnabled(false);
		display.getStopBtn().setEnabled(true);
		
		Intent serviceIntent = new Intent(MainActivity.this, FSMService.class);
		serviceIntent.fillIn(getIntent(), 0);
		Log.d(this.getClass().getCanonicalName(), "Starting service: " + FSMService.class.getCanonicalName());
		startService(serviceIntent);
		((View) findViewById(R.id.log)).setVisibility(View.VISIBLE);
		((View) findViewById(R.id.spinner)).setVisibility(View.GONE);
	}
	
	private void stopFSM() {
		display.getLogView().setVisibility(View.VISIBLE);
		display.getLoaderSpinner().setVisibility(View.GONE);
		display.getStartBtn().setEnabled(true);
		display.getStopBtn().setEnabled(false);
		
		Intent intent = new Intent(this, FSMService.class);
		stopService(intent);
	}
}