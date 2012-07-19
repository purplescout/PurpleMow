package se.purplescout.purplemow.onboard.web.service.remote;

import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM.State;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.onboard.web.service.RemoteService;

public class RemoteServiceImpl implements RemoteService {

	private static final int STEP = 85;

	private MotorFSM motorFSM;

	public RemoteServiceImpl(MotorFSM motorFSM) {
		this.motorFSM = motorFSM;
	}

	@Override
	public void incrementMovmentSpeed(Direction direction) {
		switch (direction) {
		case FORWARD:
			forward();
			break;
		case REVERSE:
			reverse();
			break;
		case LEFT:
			left();
			break;
		case RIGHT:
			right();
			break;
		}
	}

	@Override
	public void stop() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));
	}

	@Override
	public void incrementCutterSpeed() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOW, motorFSM.getCurrentCutterSpeed() + STEP));
	}

	@Override
	public void decrementCutterSpeed() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOW, motorFSM.getCurrentCutterSpeed() - STEP));
	}

	private void forward() {
		if (motorFSM.getCurrentState().equals(State.MOVING_FWD)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, STEP));
		}
	}

	private void reverse() {
		if (motorFSM.getCurrentState().equals(State.BACKING_UP)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE, STEP));
		}
	}

	private void left() {
		if (motorFSM.getCurrentState().equals(State.TURNING_LEFT)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, STEP));
		}
	}

	private void right() {
		if (motorFSM.getCurrentState().equals(State.TURNING_RIGHT)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT, STEP));
		}
	}
}
