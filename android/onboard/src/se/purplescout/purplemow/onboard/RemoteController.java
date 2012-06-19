package se.purplescout.purplemow.onboard;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.Charset;

import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM.State;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;

public class RemoteController {
	private static final int STEP = 85;
	private MotorFSM motorFSM;

	public RemoteController(MotorFSM motorFSM) {
		this.motorFSM = motorFSM;
	}
	
	public void incrementForward() {
		if (motorFSM.getCurrentState().equals(State.MOVING_FWD)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD, STEP));
		}
	}
	
	public void incrementBackward() {
		if (motorFSM.getCurrentState().equals(State.BACKING_UP)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE, STEP));
		}
	}
	
	public void incrementLeft() {
		if (motorFSM.getCurrentState().equals(State.TURNING_LEFT)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT, STEP));
		}
	}
	
	public void incrementRight() {
		if (motorFSM.getCurrentState().equals(State.TURNING_RIGHT)) {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT, motorFSM.getCurrentSpeed() + STEP));
		} else {
			motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT, STEP));
		}
	}
	
	public void stop() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));
	}
	
	public void incrementCutter() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOW, motorFSM.getCurrentCutterSpeed() + STEP));
	}
	
	public void decrementCutter() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOW, motorFSM.getCurrentCutterSpeed() - STEP));
	}
	
	//TODO move this to a more proper class
	public InputStream getMotorFSMEventLog() {
		StringBuilder logger = motorFSM.getLogger();
		return new ByteArrayInputStream(logger.toString().getBytes());
	}
}
