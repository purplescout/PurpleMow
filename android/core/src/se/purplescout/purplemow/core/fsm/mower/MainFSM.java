package se.purplescout.purplemow.core.fsm.mower;

import java.util.Random;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriberThread;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.MowerChangeStateEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEvent;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEventHandler;
import android.util.Log;

public class MainFSM extends CoreBusSubscriberThread implements StartedMowingEventHandler, BwfSensorReceiveEventHandler, RangeSensorReceiveEventHandler {

	private enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE
	}

	private State state = State.IDLE;
	private CoreBus coreBus = CoreBus.getInstance();

	public MainFSM() {
		setupSubscriptions();
	}

	private void setupSubscriptions() {
		subscribe(BwfSensorReceiveEvent.TYPE, this);
		subscribe(RangeSensorReceiveEvent.TYPE, this);
		subscribe(StartedMowingEvent.TYPE, this);
	}
	
	@Override
	public void onRangeSensorReceive(RangeSensorReceiveEvent event) {
		if (state == State.MOWING) {
			if (event.getValue() > Constants.RANGE_LIMIT) {
				changeState(State.AVOIDING_OBSTACLE);
				avoidOstacle();
			}
		}
	}

	@Override
	public void onMowerChangeState(BwfSensorReceiveEvent event) {
		if (state == State.MOWING) {
			if (event.getValue() < Constants.BWF_LIMIT) {
				changeState(State.AVOIDING_OBSTACLE);
				avoidOstacle();	
			}
		}
	}

	@Override
	public void onStartedMowing(StartedMowingEvent event) {
		changeState(State.MOWING);
	}

	private void avoidOstacle() {
		coreBus.fireEvent(new StopEvent());

		coreBus.fireDelaydEvent(new MoveEvent(Constants.FULL_SPEED, Direction.BACKWARD), 500);
		coreBus.fireDelaydEvent(new StopEvent(), 1500);
		
		if (new Random().nextBoolean()) {
			coreBus.fireDelaydEvent(new MoveEvent(Constants.FULL_SPEED, Direction.LEFT), 2000);			
		} else {
			coreBus.fireDelaydEvent(new MoveEvent(Constants.FULL_SPEED, Direction.RIGHT), 2000);
		}
		coreBus.fireDelaydEvent(new StopEvent(), 3000);
		coreBus.fireDelaydEvent(new MoveEvent(Constants.FULL_SPEED, Direction.FORWARD), 3500);
	}

	private void changeState(State newState) {
		Log.v(this.getClass().getSimpleName(), "Change state from " + state + ", to " + newState);
		coreBus.fireEvent(new MowerChangeStateEvent(state.name(), newState.name()));
		state = newState;
	}
}
