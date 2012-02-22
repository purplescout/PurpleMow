package se.purplescout.purplemow;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
			UsbComStream usbComStream = new UsbComStream(fileInputStream, fileOutputStream);
			MotorController mc = MotorController.getInstance();
			mc.setComStream(usbComStream);
			SensorReader sr = new SensorReader(usbComStream);
			// Kör igång huvudtråden
			// Thread thread = new Thread(null, new StateMachine(mc, sr), "PurpleMow");
			Thread thread = new Thread(null, new MainFsm(usbComStream), "PurpleMow");
			thread.start();
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

}
