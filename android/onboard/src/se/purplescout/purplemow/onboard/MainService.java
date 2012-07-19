package se.purplescout.purplemow.onboard;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.onboard.backend.dao.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import se.purplescout.purplemow.onboard.web.WebServer;
import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
import se.purplescout.purplemow.onboard.web.service.RemoteService;
import se.purplescout.purplemow.onboard.web.service.ScheduleService;
import se.purplescout.purplemow.onboard.web.service.remote.RemoteServiceImpl;
import se.purplescout.purplemow.onboard.web.service.schedule.ScheduleServiceImpl;
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
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

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

		LogCallback logCallback = new LogCallback() {

			@Override
			public void post(LogMessage msg) {
				Intent intent = new Intent(ACTION_LOG_MSG);
				intent.putExtra(ACTION_LOG_MSG, msg);
				MainService.this.getApplicationContext().sendBroadcast(intent);
			}
		};
		mainFSM = new MainFSM(logCallback);
		motorFSM = new MotorFSM(comStream, logCallback);
		sensorReader = new SensorReader(comStream);
		mainFSM.setMotorFSM(motorFSM);
		motorFSM.setMainFSM(mainFSM);
		sensorReader.setMainFSM(mainFSM);

		Log.d(this.getClass().getCanonicalName(), "Starting FSM");
		mainFSM.start();
		motorFSM.start();
		sensorReader.start();

		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, Constants.FULL_SPEED));

		try {
			OrmLiteSqliteOpenHelper sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
			ConnectionSource connectionSource = sqliteOpenHelper.getConnectionSource();

			RemoteService remoteService = new RemoteServiceImpl(motorFSM);
			ScheduleEventDAO scheduleEntryDAO = new ScheduleEventDAOImpl(connectionSource);
			ScheduleService scheduleService = new ScheduleServiceImpl(scheduleEntryDAO);
			RpcDispatcher dispatcher = new RpcDispatcher(remoteService, scheduleService);
			webServer = new WebServer(8080, this, dispatcher);
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
			throw new RuntimeException(e);
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
