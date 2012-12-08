package se.purplescout.purplemow.onboard;

import se.purplescout.purplemow.R;
import se.purplescout.purplemow.core.common.LogMessage;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.future.usb.UsbManager;

public class MainActivity extends Activity {
	
	public static boolean serviceRunning = false;
	
	private static final String ACTION_LOG_MSG = "se.purplescout.purplemow.LOG_MSG";
	public static final String START_MOWER = "se.purplescout.purplemow.START_MOWER";
	public static final String STOP_MOWER = "se.purplescout.purplemow.STOP_MOWER";

	public interface Display {

		Button getStartBtn();

		Button getStopBtn();

		View getLoaderSpinner();

		void showNotConnectedPopup(Runnable onCancel);

		void hideNotConnectedPopup();
		
		void setBwfLeft(String value);
		
		void setBwfRight(String value);
		
		void setRangeLeft(String value);
		
		void setRangeRight(String value);
		
		void setCurrentState(String state);
	}

	Display display;
	Intent usbAccessoryIntent;

	BroadcastReceiver logReceiver;
	BroadcastReceiver usbDetachedReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("PurpleMow", "¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ Nu startar det! ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤");
		display = new MainDisplay(this);
		bind();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(logReceiver);
		unregisterReceiver(usbDetachedReceiver);
	}

	private void bind() {
		logReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				LogMessage message = (LogMessage) intent.getExtras().getSerializable(ACTION_LOG_MSG);
				switch (message.getType()) {
				case BWF_LEFT:
					display.setBwfLeft(message.getValue());
					break;
				case BWF_RIGHT:
					display.setBwfRight(message.getValue());
					break;
				case RANGE_LEFT:
					display.setRangeLeft(message.getValue());
					break;
				case RANGE_RIGHT:
					display.setRangeRight(message.getValue());
					break;
				case CURRENT_STATE:
					display.setCurrentState(message.getValue());
					break;
				default:
					break;
				}
			}
		};
		registerReceiver(logReceiver, new IntentFilter(ACTION_LOG_MSG));
		
		usbDetachedReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
		registerReceiver(usbDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
		
		if (!serviceRunning) {
			display.showNotConnectedPopup(new Runnable() {
				
				@Override
				public void run() {
					finish();
				}
			});
		}
	}
}