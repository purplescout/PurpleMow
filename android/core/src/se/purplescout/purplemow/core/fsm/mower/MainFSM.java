package se.purplescout.purplemow.core.fsm.mower;

import java.util.Random;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriberThread;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEvent;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEventHandler;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.MowerChangeStateEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEvent;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEventHandler;
import android.util.Log;

public class MainFSM extends CoreBusSubscriberThread implements StartedMowingEventHandler, BwfSensorReceiveEventHandler, RangeSensorReceiveEventHandler, 
	BatterySensorReceiveEventHandler, NewConstantsEventHandler {

	private enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, GOING_HOME, CHARGING
	}

	private State state = State.IDLE;
	private CoreBus coreBus = CoreBus.getInstance();
	private int offset;
	private int hysteres = 0;
	private int hysterVal = 5;
	private static final int THRESHOLD1 = 15;
	private static final int THRESHOLD2 = 30;
	private Constants constants;

	public MainFSM(Constants constants) {
		this.constants = constants;
		setupSubscriptions();
	}

	private void setupSubscriptions() {
		subscribe(BwfSensorReceiveEvent.TYPE, this);
		subscribe(RangeSensorReceiveEvent.TYPE, this);
		subscribe(StartedMowingEvent.TYPE, this);
		subscribe(BatterySensorReceiveEvent.TYPE, this);
		subscribe(NewConstantsEvent.TYPE, this);
	}

	@Override
	public void onRangeSensorReceive(RangeSensorReceiveEvent event) {
		if (state == State.MOWING) {
			if (event.getValue() > constants.getRangeLimit()) {
				changeState(State.AVOIDING_OBSTACLE);
				avoidOstacle();
			}
		}
	}

	@Override
	public void onMowerChangeState(BwfSensorReceiveEvent event) {
		if (state == State.MOWING) {
			if (event.getValue() < constants.getBwfLimit()) {
				changeState(State.AVOIDING_OBSTACLE);
				avoidOstacle();
			}
		}
		if (state == State.GOING_HOME) {
			goHome(event.getValue());
		}
	}

	@Override
	public void onStartedMowing(StartedMowingEvent event) {
		if (state == State.AVOIDING_OBSTACLE || state == State.CHARGING || state == State.IDLE) {
			changeState(State.MOWING);
		}
	}

	@Override
	public void onBatterySensorReceived(BatterySensorReceiveEvent event) {
		if (state == State.MOWING) {
			if (event.getValue() <= constants.getBatteryLow())
				;
			changeState(State.GOING_HOME);
		}
		if (state == State.CHARGING && state == State.GOING_HOME) {
			if (event.getValue() >= constants.getBatteryCharged()) {
				coreBus.fireEvent(new MoveEvent(constants.getFullSpeed(), Direction.FORWARD));
				changeState(State.MOWING);
			}
		}
	}

	@Override
	public void onNewConstants(NewConstantsEvent event) {
		this.constants = event.getConstants();
	}

	private void avoidOstacle() {
		coreBus.fireEvent(new StopEvent());

		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.BACKWARD), 500);
		coreBus.fireDelaydEvent(new StopEvent(), 1500);

		if (new Random().nextBoolean()) {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.LEFT), 2000);
		} else {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.RIGHT), 2000);
		}
		coreBus.fireDelaydEvent(new StopEvent(), 3000);
		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.FORWARD), 3500);
	}

	private void goHome(int bwfReading) {
		int diff = bwfReading - offset - hysteres;
		Log.d("goHome()", "Diff is: " + diff);
		if (diff >= -THRESHOLD1 && diff <= THRESHOLD1) {
			// We are centered - drive straight
			coreBus.fireEvent(new MoveEvent(constants.getFullSpeed() / 2, Direction.FORWARD));
			hysteres = 0;
		} else {
			if (diff > 0) {
				if (diff > THRESHOLD2) {
					// Very off center: turn in place
					coreBus.fireEvent(new MoveEvent(constants.getFullSpeed(), Direction.LEFT));
				} else {
					// Slightly off center: shallow turn
					coreBus.fireEvent(new MoveEvent(constants.getFullSpeed() / 2, Direction.LEFT));
				}
				// sets hysteresis to avoid over-turning
				hysteres = hysterVal;
			} else {
				if (diff < -THRESHOLD2) {
					coreBus.fireEvent(new MoveEvent(constants.getFullSpeed(), Direction.RIGHT));
				} else {
					coreBus.fireEvent(new MoveEvent(constants.getFullSpeed() / 2, Direction.RIGHT));
				}
				hysteres = -hysterVal;
			}
		}
	}

	private void changeState(State newState) {
		Log.v(this.getClass().getSimpleName(), "Change state from " + state + ", to " + newState);
		coreBus.fireEvent(new MowerChangeStateEvent(state.name(), newState.name()));
		state = newState;
	}
}
