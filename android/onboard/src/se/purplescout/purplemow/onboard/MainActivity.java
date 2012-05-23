package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class MainActivity extends Activity {
	
	private UsbCommunicator mUsbCommunicator;
	private PendingIntent mPermissionIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView textView = (TextView) findViewById(R.id.textview);
		Log.i("PurpleMow",
				"¤¤¤¤¤¤Nu startar det!¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤");
		mUsbCommunicator = new UsbCommunicator(textView);

		mUsbCommunicator.setUsbManager(UsbManager.getInstance(this));
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(UsbCommunicator.ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(UsbCommunicator.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbCommunicator, filter);

		if (getLastNonConfigurationInstance() != null) {
			UsbAccessory accessory = (UsbAccessory) getLastNonConfigurationInstance();
			mUsbCommunicator.openAccessory(accessory);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mUsbCommunicator.resume(mPermissionIntent);
	}

	@Override
	public void onPause() {
		super.onPause();
		mUsbCommunicator.closeAccessory();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbCommunicator);
		super.onDestroy();
	}
}