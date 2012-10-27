package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity used for catching the USB_ATTACHED intent and setup MainService.
 * 
 * Only appear once and is not saved in the activity history.
 * 
 * @author lars
 *
 */
public class UsbActivity extends Activity {

	BroadcastReceiver serviceRunningReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainService.serviceRunning = false;
		setContentView(R.layout.usb);
		bind();
	}

	private void bind() {
		serviceRunningReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				startActivity(new Intent(UsbActivity.this, MainActivity.class));
			}
		};
		registerReceiver(serviceRunningReceiver, new IntentFilter(MainService.SERVICE_IS_RUNNING));
		
		startMainService();	
	}

	@Override
	protected void onDestroy() {
		super.onStop();
		unregisterReceiver(serviceRunningReceiver);
	}
	
	private void startMainService() {
		Log.d(this.getClass().getName(), "Starting service: " + MainService.class.getName());
		Intent serviceIntent = new Intent(this, MainService.class);
		serviceIntent.fillIn(getIntent(), 0);
		startService(serviceIntent);
	}
}
