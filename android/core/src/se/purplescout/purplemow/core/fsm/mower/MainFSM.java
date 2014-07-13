package se.purplescout.purplemow.core.fsm.mower;

import java.util.Random;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriberThread;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEvent;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEventHandler;
import se.purplescout.purplemow.core.fsm.motor.MotorFSM;
import se.purplescout.purplemow.core.fsm.motor.event.EmergencyStopEvent;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.core.fsm.motor.event.MowEvent;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.BumperEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BumperEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.MowerChangeStateEvent;
import se.purplescout.purplemow.core.fsm.mower.event.NoBWFDataEvent;
import se.purplescout.purplemow.core.fsm.mower.event.NoBWFDataEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.OutsideBWFEvent;
import se.purplescout.purplemow.core.fsm.mower.event.OutsideBWFEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.PushButtonPressedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.PushButtonPressedEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEvent;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEventHandler;
import android.util.Log;

public class MainFSM extends CoreBusSubscriberThread implements StartedMowingEventHandler, BwfSensorReceiveEventHandler, RangeSensorReceiveEventHandler,
	BatterySensorReceiveEventHandler, NewConstantsEventHandler, BumperEventHandler, OutsideBWFEventHandler, NoBWFDataEventHandler, PushButtonPressedEventHandler {

//	private static final int INSIDE_BWF = 107;
//	private static final int OUTSIDE_BWF = 171;
	private static final int INSIDE_BWF = 215;
	private static final int OUTSIDE_BWF = 87;
	
	private enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, GOING_HOME, CHARGING, EMERGENCY_STOPPED, STOPPED_NO_SIGNAL
	}

	private State state = State.IDLE;
	private CoreBus coreBus = CoreBus.getInstance();
	private int hysteres = 0;
	private Constants constants;
	private boolean batteryLow;
		

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
		subscribe(BumperEvent.TYPE, this);
		subscribe(OutsideBWFEvent.TYPE, this);
		subscribe(NoBWFDataEvent.TYPE, this);
		subscribe(PushButtonPressedEvent.TYPE, this);
	}

	@Override
	public void onRangeSensorReceive(RangeSensorReceiveEvent event) {

	}

	@Override
	public void onBWFSensorEvent(BwfSensorReceiveEvent event) {

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
			if (event.getValue() <= constants.getBatteryLow()) {
				coreBus.fireEvent(new EmergencyStopEvent("Battery low"));
				batteryLow = true;
				return;
			}
		}

		//Adam: Ta tillbaka när du fått det att funka med att åka hem.
		//		if (state == State.CHARGING || state == State.GOING_HOME) {
//			if (event.getValue() >= constants.getBatteryCharged()) {
//				coreBus.fireEvent(new MoveEvent(constants.getFullSpeed(), Direction.FORWARD));
//				batteryLow = false;
//				changeState(State.MOWING);
//			}
//		}
	}
	@Override
	public void onBumperPressed(BumperEvent event) {
		Log.i(this.getClass().getSimpleName(), "Bumper pressed in state " + state);
		if (state == State.MOWING) {
			changeState(State.AVOIDING_OBSTACLE);
			avoidObstacle();
		}
	}
	
	@Override
	public void onOutSide(OutsideBWFEvent event) {
		Log.i(this.getClass().getSimpleName(), "Outside BWF in state " + state);
		if (event.getBwfVal() == OUTSIDE_BWF && state == State.MOWING) {
			if(batteryLow) {
				changeState(State.GOING_HOME);
				goHome(event.getBwfVal());
				
			} else {
				changeState(State.AVOIDING_OBSTACLE);
				changeDirection();
			}
		} 				
		if (state == State.GOING_HOME) {
			goHome(event.getBwfVal());
		}
		
		if(event.getBwfVal() == INSIDE_BWF && state == State.STOPPED_NO_SIGNAL) {
			Log.i(this.getClass().getSimpleName(), "Signal is back again! Starting up.");
			enableDelayedEvents();
			int fullSpeed = constants.getFullSpeed();
			int speed60Percent = (int) (255*0.6);
			coreBus.fireEvent(new MowEvent(speed60Percent));
			coreBus.fireDelaydEvent(new MoveEvent(fullSpeed, Direction.FORWARD), 1000);
			changeState(State.MOWING);
		}
	}
	
	@Override
	public void onNoData(NoBWFDataEvent event) {
		Log.i(this.getClass().getSimpleName(), "No signal from BWF. In state " + state );
		if (state == State.MOWING || state == State.GOING_HOME || state  == State.AVOIDING_OBSTACLE) {
			Log.i(this.getClass().getSimpleName(), "Ingen data. Shutting down " + state );
			removeDelayedEvents();
			coreBus.fireEvent(new EmergencyStopEvent("Ingen data från BWF"));
			changeState(State.STOPPED_NO_SIGNAL);
		}
	}

	@Override
	public void onButtonPressed(PushButtonPressedEvent event) {
		Log.i(this.getClass().getSimpleName(), "Button pressed. In state " + state );
		if(state == State.STOPPED_NO_SIGNAL) {
			int fullSpeed = constants.getFullSpeed();
			int speed60Percent = (int) (255*0.6);
			enableDelayedEvents();
			coreBus.fireEvent(new MowEvent(speed60Percent));
			coreBus.fireEvent(new MoveEvent(fullSpeed, Direction.FORWARD));
			changeState(State.MOWING);
		}
	}

	@Override
	public void onNewConstants(NewConstantsEvent event) {
		this.constants = event.getConstants();
	}

	private void avoidObstacle() {
		Log.i(this.getClass().getSimpleName(), "Doing avoidObstacle(). Delayed events = " + getDelayedEventsStatus());
		coreBus.fireEvent(new StopEvent());

		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.BACKWARD), 300);
		coreBus.fireDelaydEvent(new StopEvent(), 1800);

		if (new Random().nextBoolean()) {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.LEFT), 2100);
		} else {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.RIGHT), 2100);
		}
		coreBus.fireDelaydEvent(new StopEvent(), 2900);
		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.FORWARD), 3200);
	}

	private void changeDirection() {
		Log.i(this.getClass().getSimpleName(), "Doing changeDirection(). RemoveDelayedEvents = " + getDelayedEventsStatus());
		coreBus.fireEvent(new StopEvent());

		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.BACKWARD), 200);
		coreBus.fireDelaydEvent(new StopEvent(), 1400);

		if (new Random().nextBoolean()) {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.LEFT), 1600);
		} else {
			coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.RIGHT), 1600);
		}
		coreBus.fireDelaydEvent(new StopEvent(), 2800);
		coreBus.fireDelaydEvent(new MoveEvent(constants.getFullSpeed(), Direction.FORWARD), 2900);
	}
	
	private void goHome(int bwfReading) {
		if (bwfReading == OUTSIDE_BWF) {
			coreBus.fireEvent(new MoveEvent(constants.getFullSpeed() / 2, 0, Direction.LEFT));
		} else if (bwfReading == INSIDE_BWF) {
			coreBus.fireEvent(new MoveEvent(0, constants.getFullSpeed() / 2, Direction.RIGHT));
		}
		
	}

	private void changeState(State newState) {
		Log.v(this.getClass().getSimpleName(), "Change state from " + state + ", to " + newState);
		coreBus.fireEvent(new MowerChangeStateEvent(state.name(), newState.name()));
		state = newState;
	}
	
}
