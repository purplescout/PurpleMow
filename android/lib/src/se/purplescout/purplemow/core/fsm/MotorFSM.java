package se.purplescout.purplemow.core.fsm;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.MotorController;
import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent.EventType;
import android.util.Log;

public class MotorFSM extends AbstractFSM<MotorFSMEvent> {

	public enum State {
		STOPPED, MOVING_FWD, TURNING_LEFT, BACKING_UP, TURNING_RIGHT
	}

	private State state = State.STOPPED;
	private MotorController motorController;
	private final LogCallback logCallback;
	private AbstractFSM<MainFSMEvent> mainFSM;
	private int currentMovementSpeed;
	private int currentCutterSpeed;
	private int thrownExceptions = 0;

	@Override
	public void shutdown() {
		try {
			stopMotors();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
		} finally {
			super.shutdown();
		}
	}

	public MotorFSM(ComStream comStream, LogCallback logCallback) {
		this.logCallback = logCallback;
		this.motorController = new MotorController(comStream);
	}

	public void setMainFSM(AbstractFSM<MainFSMEvent> fsm) {
		this.mainFSM = fsm;
	}
	
	public int getCurrentSpeed() {
		return currentMovementSpeed;
	}
	
	public int getCurrentCutterSpeed() {
		return currentCutterSpeed;
	}
	
	public State getCurrentState() {
		return state;
	}

	@Override
	protected void handleEvent(MotorFSMEvent event) {
		int value = event.getValue();
		if (value > Constants.FULL_SPEED) {
			value = Constants.FULL_SPEED;
		} else if (value < 0) {
			value = 0;
		}

		try {
			switch (event.getEventType()) {
			case MOVE_FWD:
				moveForward(value);
				mainFSM.queueEvent(new MainFSMEvent(MainFSMEvent.EventType.STARTED_MOWING));
				break;
			case REVERSE:
				backUp(value);
				break;
			case TURN_LEFT:
				turnLeft(value);
				break;
			case TURN_RIGHT:
				turnRight(value);
				break;
			case STOP:
				stopMotors();
				break;
			case EMERGENCY_STOP:
				stopMotors();
				System.exit(1);
				break;
			case MOW:
				cutterEngine(value);
			default:
				break;
			}
		} catch (IOException e) {
			// Prevents flooding the log
			if (thrownExceptions < 2) {
				Log.e(this.getClass().getName(), e.getMessage(), e);
				thrownExceptions++;
			}
		}
	}

	private void stopMotors() throws IOException {
		motorController.move(0);
		changeState(State.STOPPED);
	}

	private void moveForward(int value) throws IOException {
		if (state == State.STOPPED || state == State.MOVING_FWD) {
			motorController.setDirection(Direction.FORWARD);
			move(value);
			changeState(State.MOVING_FWD);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.MOVE_FWD, value), 500);
		}
	}

	private void backUp(int value) throws IOException {
		if (state == State.STOPPED || state == State.BACKING_UP) {
			motorController.setDirection(Direction.BACKWARD);
			move(value);
			changeState(State.BACKING_UP);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.REVERSE, value), 500);
		}
	}

	private void turnLeft(int value) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_LEFT) {
			motorController.setDirection(Direction.LEFT);
			move(value);
			changeState(State.TURNING_LEFT);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_LEFT, value), 500);
		}
	}

	private void turnRight(int value) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_RIGHT) {
			motorController.setDirection(Direction.RIGHT);
			move(value);
			changeState(State.TURNING_RIGHT);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_RIGHT, value), 500);
		}
	}

	private void move(int value) throws IOException {
		motorController.move(value);
		currentMovementSpeed = value;
	}

	private void cutterEngine(int value) throws IOException {
		motorController.runCutter(value);
		currentCutterSpeed = value;
	}

	private void changeState(State newState) {
		state = newState;
	}
}
