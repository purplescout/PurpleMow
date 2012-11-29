package se.purplescout.purplemow.core.fsm;

import java.util.Random;

import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.LogMessage.Type;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import android.util.Log;

public class MainFSM extends AbstractFSM<MainFSMEvent> {

	private enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, GOING_HOME, CHARGING
	}

	private State state = State.IDLE;
	private AbstractFSM<MotorFSMEvent> motorFSM;
	private LogCallback logCallback;
	private boolean batteryLow;

	public MainFSM(LogCallback log) {
		this.logCallback = log;
	}

	@Override
	protected void handleEvent(MainFSMEvent event) {
		if(event.getEventType() == EventType.BATTERY_LOW) {
			batteryLow=true;
		}
		switch (state) {
		case IDLE:
			if (event.getEventType() == MainFSMEvent.EventType.STARTED_MOWING) {
				changeState(State.MOWING);
			}
			break;
		case MOWING:
			if (event.getEventType() == EventType.RANGE_LEFT) {
				logCallback.post(LogMessage.create(Type.RANGE_LEFT, Integer.toString(event.getValue())));
				if (event.getValue() > Constants.RANGE_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle(event.getEventType().name());
				}
			} else if (event.getEventType() == EventType.RANGE_RIGHT) {
				logCallback.post(LogMessage.create(Type.RANGE_RIGHT, Integer.toString(event.getValue())));
				if (event.getValue() > Constants.RANGE_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle(event.getEventType().name());
				}
			} else if (event.getEventType() == EventType.BWF_RIGHT) {
				logCallback.post(LogMessage.create(Type.BWF_RIGHT, Integer.toString(event.getValue())));
				if (event.getValue() < Constants.BWF_LIMIT) {
					if(batteryLow) {
						changeState(State.GOING_HOME);
						goHome();
					} else {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle(event.getEventType().name());
					}
				}
			} else if (event.getEventType() == EventType.BWF_LEFT) {
				logCallback.post(LogMessage.create(Type.BWF_LEFT, Integer.toString(event.getValue())));
				if (event.getValue() < Constants.BWF_LIMIT) {
					if(batteryLow) {
						changeState(State.GOING_HOME);
						goHome();
					} else {
						changeState(State.AVOIDING_OBSTACLE);
						avoidOstacle(event.getEventType().name());
					}
				}
			}
			break;
		case AVOIDING_OBSTACLE:
			if(event.getEventType().equals(EventType.BWF_LEFT)) {
				logCallback.post(LogMessage.create(Type.BWF_LEFT, Integer.toString(event.getValue())));
			} 
			if (event.getEventType() == EventType.STARTED_MOWING) {
				changeState(State.MOWING);
			}
			break;
		case GOING_HOME:
			if (event.getEventType() == EventType.CHARGER_CONNECTED) {
				changeState(State.CHARGING);
			}
		}

	}

	private void avoidOstacle(String cause) {
		logCallback.post(LogMessage.create(Type.CURRENT_STATE, " due to " + cause));
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));

		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE, Constants.FULL_SPEED), 500);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP), 1500);
		if (new Random().nextBoolean()) {
			motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, Constants.FULL_SPEED), 2000);
		} else {
			motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT, Constants.FULL_SPEED), 2000);
		}
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP), 3000);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, Constants.FULL_SPEED), 3500);
	}

	/**
	 * Routing for following the BWF cable until a
	 */
	private void goHome() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, Constants.FULL_SPEED), 500);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP), 4000);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, Constants.FULL_SPEED), 4500);
	}

	public void setMotorFSM(AbstractFSM<MotorFSMEvent> fsm) {
		motorFSM = fsm;
	}

	private void changeState(State newState) {
		Log.v(this.getClass().getSimpleName(), "Change state from " + state + ", to " + newState);
		logCallback.post(LogMessage.create(Type.CURRENT_STATE, newState.toString()));
		state = newState;
	}
}
