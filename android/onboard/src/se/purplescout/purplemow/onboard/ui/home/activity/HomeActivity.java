package se.purplescout.purplemow.onboard.ui.home.activity;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.backend.core.MainService;
import se.purplescout.purplemow.onboard.ui.configure.place.ConfigurePlace;
import se.purplescout.purplemow.onboard.ui.controller.PlaceController;
import se.purplescout.purplemow.onboard.ui.home.view.HomeView;
import se.purplescout.purplemow.onboard.ui.remote.place.RemotePlace;
import se.purplescout.purplemow.onboard.ui.schedule.place.SchedulePlace;
import se.purplescout.purplemow.onboard.ui.sensors.place.SensorsPlace;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class HomeActivity extends Activity {

	private static final boolean IN_NORMAL_MODE = false;

	private static final boolean IN_DEBUG_MODE = true;

	public static boolean serviceRunning = false;

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	public interface ViewDisplay {

		Button getRemoteBtn();

		Button getScheduleBtn();

		Button getConfigureBtn();

		Button getSensorsBtn();

		Button getLogsBtn();

		Button getSettingsBtn();

		Button getStartBtn();

		void setLoading(boolean b);

		void showNoUsbDialog(Context context, android.content.DialogInterface.OnClickListener clickListener);
	}

	ViewDisplay display;
	BroadcastReceiver serviceRunningReceiver;
	BroadcastReceiver usbReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		display = new HomeView(this);
		bind();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (serviceRunningReceiver != null) {
			unregisterReceiver(serviceRunningReceiver);
		}
		if (usbReceiver != null) {
			unregisterReceiver(usbReceiver);
		}
	}

	private void bind() {
		display.getRemoteBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PlaceController.goTo(HomeActivity.this, new RemotePlace());
			}
		});
		display.getScheduleBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PlaceController.goTo(HomeActivity.this, new SchedulePlace());
			}
		});
		display.getConfigureBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PlaceController.goTo(HomeActivity.this, new ConfigurePlace());
			}
		});
		display.getSensorsBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PlaceController.goTo(HomeActivity.this, new SensorsPlace());
			}
		});
		display.getStartBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (serviceRunning) {
					stopMainService();
				} else {
					setupService();
				}
			}
		});
	}

	private void setupService() {
		if (usbIsConnected()) {
			ensurePermissionAndRunService();
		} else {
			display.showNoUsbDialog(this, new android.content.DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startMainService(IN_DEBUG_MODE);
				}
			});
		}
	}

	private void ensurePermissionAndRunService() {
		UsbManager usbManager = UsbManager.getInstance(this);
		UsbAccessory[] usbAccessories = usbManager.getAccessoryList();
		UsbAccessory usbAccessory = usbAccessories[0];
		usbReceiver = new BroadcastReceiver() {

		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (ACTION_USB_PERMISSION.equals(action)) {
		            synchronized (this) {
		                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
		                    startMainService(IN_NORMAL_MODE);
		                }
		            }
		        }
		    }
		};
		registerReceiver(usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));

		usbManager.requestPermission(usbAccessory, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
	}

	private boolean usbIsConnected() {
		UsbManager usbManager = UsbManager.getInstance(this);
		UsbAccessory[] usbAccessories = usbManager.getAccessoryList();
		if (usbAccessories == null || usbAccessories.length == 0) {
			return false;
		}

		return true;
	}

	private void startMainService(boolean inDebugMode) {
		serviceRunningReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				display.getStartBtn().setText(HomeActivity.this.getString(R.string.stop_mower));
				display.setLoading(false);
			}
		};
		registerReceiver(serviceRunningReceiver, new IntentFilter(MainService.SERVICE_IS_RUNNING));
		Log.d(this.getClass().getSimpleName(), "Starting service: " + MainService.class.getSimpleName());
		Intent serviceIntent = new Intent(this, MainService.class);
		serviceIntent.putExtra("Debug", inDebugMode);
		startService(serviceIntent);
		display.setLoading(true);
	}

	private void stopMainService() {
		serviceRunningReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				display.getStartBtn().setText(HomeActivity.this.getString(R.string.start_mower));
				display.setLoading(false);
			}
		};
		registerReceiver(serviceRunningReceiver, new IntentFilter(MainService.SERVICE_IS_FINISHED));
		Log.d(this.getClass().getSimpleName(), "Stoping service: " + MainService.class.getSimpleName());
		Intent serviceIntent = new Intent(this, MainService.class);
		serviceIntent.fillIn(getIntent(), 0);
		stopService(serviceIntent);
		display.setLoading(true);
	}
}