package se.purplescout.purplemow.onboard.ui.sensors.activity;

import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.MowerChangeStateEvent;
import se.purplescout.purplemow.core.fsm.mower.event.MowerChangeStateEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent.Side;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEventHandler;
import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.CoreBusSubscriberActivity;
import se.purplescout.purplemow.onboard.ui.sensors.view.SensorsView;
import android.os.Bundle;

public class SensorsActivity extends CoreBusSubscriberActivity {

	public interface ViewDisplay {

		void setBwfValue(String value);

		void setLeftRangeValue(String value);

		void setRightRangeValue(String value);

		void setCurrentState(String state);
	}

	ViewDisplay display;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sensors);
		display = new SensorsView(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		subscribe(BwfSensorReceiveEvent.TYPE, new BwfSensorReceiveEventHandler() {
			
			@Override
			public void onMowerChangeState(BwfSensorReceiveEvent event) {
				String value = Integer.toString(event.getValue());
				display.setBwfValue(value);
			}
		});
		subscribe(RangeSensorReceiveEvent.TYPE, new RangeSensorReceiveEventHandler() {
			
			@Override
			public void onRangeSensorReceive(RangeSensorReceiveEvent event) {
				String value = Integer.toString(event.getValue());
				if (event.getSide() == Side.LEFT) {
					display.setLeftRangeValue(value);
				} else {
					display.setRightRangeValue(value);
				}
			}
		});
		subscribe(MowerChangeStateEvent.TYPE, new MowerChangeStateEventHandler() {
			
			@Override
			public void onMowerChangeState(MowerChangeStateEvent event) {
				display.setCurrentState(event.getNewState());
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		unsubscribeAll();
	}	
}
