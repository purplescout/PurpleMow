package se.purplescout.purplemow.onboard;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.controller.CoreController;
import se.purplescout.purplemow.core.controller.CoreControllerImpl;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.backend.service.log.LogService;
import se.purplescout.purplemow.onboard.backend.service.log.LogServiceImpl;
import se.purplescout.purplemow.onboard.backend.service.remote.RemoteService;
import se.purplescout.purplemow.onboard.backend.service.remote.RemoteServiceImpl;
import se.purplescout.purplemow.onboard.backend.service.schedule.ScheduleService;
import se.purplescout.purplemow.onboard.backend.service.schedule.ScheduleServiceImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import se.purplescout.purplemow.onboard.ui.home.activity.HomeActivity;
import se.purplescout.purplemow.onboard.web.WebServer;
import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
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
	public static final String SERVICE_IS_FINISHED = "se.purplescout.purplemow.SERVICE_IS_FINISHED";
	public static boolean serviceRunning;

	CoreController coreController = new CoreControllerImpl();
	CoreBus coreBus = CoreBus.getInstance();
	ComStream comStream;

	WebServer webServer;
	ScheduledExecutorService scheduler;
	
	BroadcastReceiver usbDetachedReceiver;

	public MainService() {
		super("se.purplescout.purplemow");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(this.getClass().getSimpleName(), "onHandleIntent");
		usbDetachedReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				stopSelf();
			}
		};
		registerReceiver(usbDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
		
		UsbManager usbManager = UsbManager.getInstance(getApplicationContext());
		UsbAccessory accessory = usbManager.getAccessoryList()[0];
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
//		comStream = new DebugComStream();
		Log.d(this.getClass().getSimpleName(), "Created usb stream: " + comStream.toString());

		setupNotification();
		setupContext();

		HomeActivity.serviceRunning = true;		
		sendBroadcast(new Intent(SERVICE_IS_RUNNING));
		
		// To keep service alive
		while (true) {
			Thread.yield();
		}
	}

	private void setupNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(se.purplescout.purplemow.onboard.R.drawable.ic_statusbar, "Purple Mow", System.currentTimeMillis());
		Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
		
		notification.setLatestEventInfo(getApplicationContext(), "PurpleMow", "", contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(NOTIFICATION_FLAG, notification);
	}

	private void setupContext() {
		Log.d(this.getClass().getSimpleName(), "Startup");

		scheduler = Executors.newScheduledThreadPool(1);
		coreController.prepare(comStream);

		Log.d(this.getClass().getSimpleName(), "Starting FSM");
		coreController.start();

		coreBus.fireEvent(new MoveEvent(Constants.FULL_SPEED, Direction.FORWARD));
		try {
			OrmLiteSqliteOpenHelper sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
			ConnectionSource connectionSource = sqliteOpenHelper.getConnectionSource();

			RemoteService remoteService = new RemoteServiceImpl();
			ScheduleEventDAO scheduleEntryDAO = new ScheduleEventDAOImpl(connectionSource);
			ScheduleService scheduleService = new ScheduleServiceImpl(scheduleEntryDAO, scheduler);
			LogService logService = new LogServiceImpl(coreController);
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
		
		if (usbDetachedReceiver != null) {
			unregisterReceiver(usbDetachedReceiver);
		}
		
		tearDownContext();
		tearDownNotification();
		
		HomeActivity.serviceRunning = false;
		sendBroadcast(new Intent(SERVICE_IS_FINISHED));
	}

	private void tearDownNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_FLAG);
	}

	private void tearDownContext() {
		scheduler.shutdown();

		webServer.stop();

		coreController.shutdown();
	}
}
