package se.purplescout.purplemow.onboard;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.fsm.MainFSM;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class UsbCommunicator extends BroadcastReceiver {
	private static final String TAG = "PurpleMow";
	static final String ACTION_USB_PERMISSION = "se.purplescout.purplemow.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream fileInputStream;
	FileOutputStream fileOutputStream;
	private boolean mPermissionRequestPending;

	private TextView textView;

	MainFSM mainFSM;

	public UsbCommunicator(TextView textView) {
		this.textView = textView;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		log("onReceive", action);
		if (ACTION_USB_PERMISSION.equals(action)) {
			synchronized (this) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);

				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					log("PurpleMow", "Access beviljad " + accessory);
					openAccessory(accessory);
				} else {

					log("PurpleMow", "permission denied for accessory " + accessory);
				}
				mPermissionRequestPending = false;
			}
		} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			UsbAccessory accessory = UsbManager.getAccessory(intent);
			if (accessory != null && accessory.equals(mAccessory)) {
				closeAccessory();
			}
		} else {
			log("onReceive", "no hit");
		}
	}

	private void log(String tag, String message) {
		textView.append(tag + " : " + message + "\n");
	}

	void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			fileInputStream = new FileInputStream(fd);
			fileOutputStream = new FileOutputStream(fd);

			// Kör igång huvudtråden
			mainFSM = new MainFSM(getComStream(), textView);
			mainFSM.start();
		}
	}

	void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
			Log.w(this.getClass().getSimpleName(), e.getMessage(), e);
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
			fileOutputStream = null;
			fileInputStream = null;
			mainFSM.stop();
		}
	}

	void setUsbManager(UsbManager usbManager) {
		mUsbManager = usbManager;
	}

	void resume(PendingIntent pendingIntent) {
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (this) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, pendingIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		}
	}

	public ComStream getComStream() {
		UsbComStream comStream = new UsbComStream(fileInputStream, fileOutputStream);
		return comStream;
	}
}
