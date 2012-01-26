package se.purpleout.purplemow;

import se.purpleout.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class MainActivity extends Activity {
	private UsbCommunicator mUsbCommunicator;
	private PendingIntent mPermissionIntent;
	private RemoteController mRemoteController;
	private WifiManager wifi;
	private WifiManager.MulticastLock mcLock;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView textView = (TextView) findViewById(R.id.textview);
		mUsbCommunicator = new UsbCommunicator(textView);

		mUsbCommunicator.setUsbManager(UsbManager.getInstance(this));
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				UsbCommunicator.ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(
				UsbCommunicator.ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbCommunicator, filter);

		if (getLastNonConfigurationInstance() != null) {
			UsbAccessory accessory = (UsbAccessory) getLastNonConfigurationInstance();
			mUsbCommunicator.openAccessory(accessory);
		}

		mRemoteController = new RemoteController(mUsbCommunicator, textView);
	}

	@Override
	public void onResume() {
		super.onResume();
		mUsbCommunicator.resume(mPermissionIntent);
		mcLock.acquire(); // tells android to process multicast packets
		mRemoteController.start((TextView) findViewById(R.id.textview));
	}

	@Override
	public void onPause() {
		super.onPause();
		mUsbCommunicator.closeAccessory();
		mcLock.release(); // stop processing packets
		mRemoteController.halt();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbCommunicator);
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mcLock = wifi.createMulticastLock("remoteControlReceiver");
		mRemoteController.start((TextView) findViewById(R.id.textview));
	}

}