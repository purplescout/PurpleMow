package se.purpleout.purplemow;

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

public class UsbCommunicator extends BroadcastReceiver implements Runnable,
		IRobot {
	private static final String TAG = "PurpleMow";
	static final String ACTION_USB_PERMISSION = "se.purplescout.purplemow.action.USB_PERMISSION";
	private static final byte SERVO1 = 1;
	private static final byte SERVO2 = 2;
	private static final byte SERVO3 = 0x12;
	private static final byte RELAY1 = 0;
	private static final byte RELAY2 = 1;

	private UsbManager mUsbManager;
	private UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	private boolean mPermissionRequestPending;
	private boolean mRelaysOn = false;

	private static final byte SERVO_COMMAND = 2;
	private static final byte RELAY_COMMAND = 3;

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
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					openAccessory(accessory);
				} else {

					log("PurpleMow", "permission denied for accessory "
							+ accessory);
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

	@Override
	public boolean isConnected() {
		return mOutputStream != null;
	}

	@Override
	public void setSensorListener(ISensorListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveForward() {
		if (mRelaysOn) {
			sendCommand(RELAY_COMMAND, RELAY1, 0);
			sendCommand(RELAY_COMMAND, RELAY2, 0);
			mRelaysOn = false;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendCommand(SERVO_COMMAND, SERVO1, 255);
		sendCommand(SERVO_COMMAND, SERVO2, 255);
	}

	@Override
	public void moveBackward() {
		if (!mRelaysOn) {
			sendCommand(RELAY_COMMAND, RELAY1, 1);
			sendCommand(RELAY_COMMAND, RELAY2, 1);
			mRelaysOn = true;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendCommand(SERVO_COMMAND, SERVO1, 255);
		sendCommand(SERVO_COMMAND, SERVO2, 255);
	}

	@Override
	public void turnLeft() {
		if (mRelaysOn) {
			sendCommand(SERVO_COMMAND, SERVO2, 255);
		} else {
			sendCommand(SERVO_COMMAND, SERVO1, 255);
		}
	}

	@Override
	public void turnRight() {
		if (mRelaysOn) {
			sendCommand(SERVO_COMMAND, SERVO1, 255);
		} else {
			sendCommand(SERVO_COMMAND, SERVO2, 255);
		}
	}

	@Override
	public void stop() {
		sendCommand(SERVO_COMMAND, SERVO1, 0);
		sendCommand(SERVO_COMMAND, SERVO2, 0);
	}

	@Override
	public void startCutter() {
		sendCommand(SERVO_COMMAND, SERVO3, 255);
	}

	@Override
	public void stopCutter() {
		sendCommand(SERVO_COMMAND, SERVO3, 0);
	}

	void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "PurpleMow");
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
			mOutputStream = null;
			mInputStream = null;
		}
	}

	@Override
	public void run() {
		int ret = 0;
		final byte[] buffer = new byte[16384];
		int i;

		while (ret >= 0) {
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;
				textView.post(new Runnable() {

					@Override
					public void run() {
						textView.setText("[" + Byte.toString(buffer[0]) + ","
								+ Byte.toString(buffer[1]) + ","
								+ Byte.toString(buffer[2]) + ","
								+ Byte.toString(buffer[3]) + "]\n");
					}
				});
				switch (buffer[i]) {
				case 0x1:
				case 0x4:
				case 0x5:
				case 0x6:
				default:
					Log.d(TAG, "unknown msg: " + buffer[i]);
					i = len;
					break;
				}
			}

		}
	}

	public void sendCommand(byte command, byte target, int value) {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		Log.w(this.getClass().getSimpleName(),
				"[" + Byte.toString(buffer[0]) + "," + Byte.toString(buffer[1])
						+ "," + Byte.toString(buffer[2]) + "]\n");
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
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

	@Override
	public void readSensor() {
		sendCommand((byte)4, (byte)0, (byte)1);
	}
	
}
