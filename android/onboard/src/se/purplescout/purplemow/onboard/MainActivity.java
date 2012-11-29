package se.purplescout.purplemow.onboard;

import se.purplescout.R;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import se.purplescout.purplemow.onboard.MainService.LocalBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.future.usb.UsbManager;

public class MainActivity extends Activity {
	
	public static boolean serviceRunning = false;
	public static boolean batteryLevelLow = false;
	
	private static final String ACTION_LOG_MSG = "se.purplescout.purplemow.LOG_MSG";
	public static final String START_MOWER = "se.purplescout.purplemow.START_MOWER";
	public static final String STOP_MOWER = "se.purplescout.purplemow.STOP_MOWER";
    private boolean mBound;

	public interface Display {

		Button getStartBtn();

		Button getStopBtn();

		View getLoaderSpinner();

		void showNotConnectedPopup(Runnable onCancel);

		void hideNotConnectedPopup();
		
		void setBwfLeft(String value);
		
		void setBwfRight(String value);
		
		void setRangeLeft(String value);
		
		void setRangeRight(String value);
		
		void setCurrentState(String state);

		CheckBox getBattLowBtn();
	}

	Display display;
	Intent usbAccessoryIntent;

	BroadcastReceiver logReceiver;
	BroadcastReceiver usbDetachedReceiver;
	MainService mainService;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Log.i("PurpleMow", "¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤ Nu startar det! ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤");
		display = new MainDisplay(this);
		bind();
		bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(logReceiver);
		unregisterReceiver(usbDetachedReceiver);
	}

	private void bind() {

//		display.getBattLowBtn().setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				 boolean checked = ((CheckBox) buttonView).isChecked();
//				    if(R.id.battLow == buttonView.getId()) {
//				    	if(checked){
//				    		mainService.postEventOnMainFSM(new MainFSMEvent(EventType.BATTERY_LOW));
//				    	}
//				    }	
//
//			}
//		});
//		
//		display.getBattLowBtn().setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View buttonView) {
//				mainService.postEventOnMainFSM(new MainFSMEvent(EventType.BATTERY_LOW));
//			}
//		});
//		
		logReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				LogMessage message = (LogMessage) intent.getExtras().getSerializable(ACTION_LOG_MSG);
				switch (message.getType()) {
				case BWF_LEFT:
					display.setBwfLeft(message.getValue());
					break;
				case BWF_RIGHT:
					display.setBwfRight(message.getValue());
					break;
				case RANGE_LEFT:
					display.setRangeLeft(message.getValue());
					break;
				case RANGE_RIGHT:
					display.setRangeRight(message.getValue());
					break;
				case CURRENT_STATE:
					display.setCurrentState(message.getValue());
					break;
				default:
					break;
				}
			}
		};
		registerReceiver(logReceiver, new IntentFilter(ACTION_LOG_MSG));
		
		usbDetachedReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				finish();
			}
		};
		registerReceiver(usbDetachedReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
		
		if (!serviceRunning) {
			display.showNotConnectedPopup(new Runnable() {
				
				@Override
				public void run() {
					finish();
				}
			});
		}

	}
	 /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mainService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    public void onCheckboxClicked(View view) {
    	mainService.postEventOnMainFSM(new MainFSMEvent(EventType.BATTERY_LOW));   	
    }

}