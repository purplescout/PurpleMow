package se.purplescout.purplemow.onboard;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import se.purplescout.purplemow.onboard.web.WebServer;
import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
import se.purplescout.purplemow.onboard.web.service.log.LogService;
import se.purplescout.purplemow.onboard.web.service.log.LogServiceImpl;
import se.purplescout.purplemow.onboard.web.service.remote.RemoteService;
import se.purplescout.purplemow.onboard.web.service.remote.RemoteServiceImpl;
import se.purplescout.purplemow.onboard.web.service.schedule.ScheduleService;
import se.purplescout.purplemow.onboard.web.service.schedule.ScheduleServiceImpl;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class MainService extends IntentService {

	private static final int NOTIFICATION_FLAG = 0;
	private static final String ACTION_LOG_MSG = "se.purplescout.purplemow.LOG_MSG";
	public static final String SERVICE_IS_RUNNING = "se.purplescout.purplemow.SERVICE_IS_RUNNING";
	public static boolean serviceRunning;

	MainFSM mainFSM;
	MotorFSM motorFSM;
	SensorReader sensorReader;
	UsbComStream comStream;

	WebServer webServer;
	ScheduledExecutorService scheduler;
	
	BroadcastReceiver usbDetachedReceiver;

	public MainService() {
		super("se.purplescout.purplemow");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(this.getClass().getSimpleName(), "onHandleIntent");
		UsbManager usbManager = UsbManager.getInstance(getApplicationContext());

		usbDetachedReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				stopSelf();
			}
		};
		registerReceiver(usbDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
		
		UsbAccessory accessory = UsbManager.getAccessory(intent);
		if (accessory == null) {
			Log.e(this.getClass().getSimpleName(), "UsbAccessory is null");
			throw new RuntimeException("UsbAccessory is null");
		} else {
			Log.d(this.getClass().getSimpleName(), "Created UsbAccessory " + accessory.getDescription() + ", " + accessory.getManufacturer());
		}
		ParcelFileDescriptor fileDescriptor = usbManager.openAccessory(accessory);
		if (fileDescriptor == null) {
			Log.e(this.getClass().getSimpleName(), "ParcelFileDescriptor is null");
			throw new RuntimeException("ParcelFileDescriptor is null");
		}

		FileDescriptor fd = fileDescriptor.getFileDescriptor();
		FileInputStream fileInputStream = new FileInputStream(fd);
		FileOutputStream fileOutputStream = new FileOutputStream(fd);

		comStream = new UsbComStream(fileInputStream, fileOutputStream);
		Log.d(this.getClass().getSimpleName(), "Created usb stream: " + comStream.toString());

		setupNotification();
		setupContext();

		MainActivity.serviceRunning = true;
		
		sendBroadcast(new Intent(SERVICE_IS_RUNNING));
		
		// To keep service alive
		while (true) {
			Thread.yield();
		}
	}

	private void setupNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(se.purplescout.R.drawable.ic_statusbar, "Purple Mow", System.currentTimeMillis());
		Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
		
		notification.setLatestEventInfo(getApplicationContext(), "PurpleMow", "", contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(NOTIFICATION_FLAG, notification);
	}

	private void setupContext() {
		Log.d(this.getClass().getSimpleName(), "Startup");

		LogCallback logCallback = new LogCallback() {

			@Override
			public void post(LogMessage msg) {
				Intent intent = new Intent(ACTION_LOG_MSG);
				intent.putExtra(ACTION_LOG_MSG, msg);
				MainService.this.getApplicationContext().sendBroadcast(intent);
			}
		};

		scheduler = Executors.newScheduledThreadPool(1);
		mainFSM = new MainFSM(logCallback);
		motorFSM = new MotorFSM(comStream, logCallback);
		sensorReader = new SensorReader(comStream);
		mainFSM.setMotorFSM(motorFSM);
		motorFSM.setMainFSM(mainFSM);
		sensorReader.setMainFSM(mainFSM);

		Log.d(this.getClass().getSimpleName(), "Starting FSM");
		mainFSM.start();
		motorFSM.start();
		sensorReader.start();

		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, Constants.FULL_SPEED));
		try {
			OrmLiteSqliteOpenHelper sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
			ConnectionSource connectionSource = sqliteOpenHelper.getConnectionSource();

			RemoteService remoteService = new RemoteServiceImpl(motorFSM);
			ScheduleEventDAO scheduleEntryDAO = new ScheduleEventDAOImpl(connectionSource);
			ScheduleService scheduleService = new ScheduleServiceImpl(scheduleEntryDAO, scheduler, motorFSM);
			LogService logService = new LogServiceImpl(sensorReader);
			RpcDispatcher dispatcher = new RpcDispatcher(remoteService, scheduleService, logService);
			webServer = new WebServer(8080, this, dispatcher);
			scheduleService.initScheduler();
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(this.getClass().getSimpleName(), "Destroy");
		
		unregisterReceiver(usbDetachedReceiver);
		
		tearDownContext();
		tearDownNotification();
		
		MainActivity.serviceRunning = false;
	}

	private void tearDownNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_FLAG);
	}

	private void tearDownContext() {
		scheduler.shutdown();

		webServer.stop();

		mainFSM.shutdown();
		motorFSM.shutdown();
		sensorReader.shutdown();
	}
}
