package se.purplescout.purplemow.onboard;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.onboard.web.WebServer;
import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class MainService extends IntentService {

	private static final int NOTIFICATION_FLAG = 0;
	private static final String ACTION_LOG_MSG = "se.purplescout.purplemow.LOG_MSG";

	MainFSM mainFSM;
	MotorFSM motorFSM;
	SensorReader sensorReader;
	UsbComStream comStream;

	WebServer webServer;

	public MainService() {
		super("se.purplescout.purplemow");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(this.getClass().getCanonicalName(), "onHandleIntent");
		UsbManager usbManager = UsbManager.getInstance(getApplicationContext());

		UsbAccessory accessory = UsbManager.getAccessory(intent);
		if (accessory == null) {
			Log.e(this.getClass().getCanonicalName(), "UsbAccessory is null");
			throw new RuntimeException("UsbAccessory is null");
		} else {
			Log.d(this.getClass().getCanonicalName(), "Created UsbAccessory " + accessory.getDescription() + ", " + accessory.getManufacturer());
		}
		ParcelFileDescriptor fileDescriptor = usbManager.openAccessory(accessory);
		if (fileDescriptor == null) {
			Log.e(this.getClass().getCanonicalName(), "ParcelFileDescriptor is null");
			throw new RuntimeException("ParcelFileDescriptor is null");
		}

		FileDescriptor fd = fileDescriptor.getFileDescriptor();
		FileInputStream fileInputStream = new FileInputStream(fd);
		FileOutputStream fileOutputStream = new FileOutputStream(fd);

		comStream = new UsbComStream(fileInputStream, fileOutputStream);
		Log.d(this.getClass().getCanonicalName(), "Created usb stream: " + comStream.toString());

		run();
	}

	private void run() {
		Log.d(this.getClass().getCanonicalName(), "Startup");
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(R.drawable.stat_notify_more, "Mower is running", System.currentTimeMillis());
		Intent notificationIntent = new Intent(getApplicationContext(), MainService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

		notification.setLatestEventInfo(getApplicationContext(), "PurpleMow", "", contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(NOTIFICATION_FLAG, notification);

		GuiLogCallback logCallback = new GuiLogCallback() {

			@Override
			public void post(String msg) {
				Intent intent = new Intent(ACTION_LOG_MSG);
				intent.putExtra(ACTION_LOG_MSG, msg);
				MainService.this.getApplicationContext().sendBroadcast(intent);
			}
		};
		mainFSM = new MainFSM(logCallback);
		motorFSM = new MotorFSM(comStream, logCallback);
		sensorReader = new SensorReader(comStream, logCallback);
		mainFSM.setMotorFSM(motorFSM);
		motorFSM.setMainFSM(mainFSM);
		sensorReader.setMainFSM(mainFSM);

		Log.d(this.getClass().getCanonicalName(), "Starting FSM");
		mainFSM.start();
		motorFSM.start();
		sensorReader.start();

		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD));

		try {
			webServer = new WebServer(8080, this, mainFSM, motorFSM);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		// To keep service alive
		while (true) {
			Thread.yield();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(this.getClass().getCanonicalName(), "Destroy");

		webServer.stop();

		mainFSM.shutdown();
		motorFSM.shutdown();
		sensorReader.shutdown();

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_FLAG);
	}
}
