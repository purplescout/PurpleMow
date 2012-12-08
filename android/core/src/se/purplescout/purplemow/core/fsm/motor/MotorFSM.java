package se.purplescout.purplemow.core.fsm.motor;

import java.io.IOException;

import se.purplescout.purplemow.core.MotorController;
import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriberThread;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.motor.event.EmergencyStopEvent;
import se.purplescout.purplemow.core.fsm.motor.event.EmergencyStopEventHandler;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEvent;
import se.purplescout.purplemow.core.fsm.motor.event.MoveEventHandler;
import se.purplescout.purplemow.core.fsm.motor.event.MowEvent;
import se.purplescout.purplemow.core.fsm.motor.event.MowEventHandler;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.core.fsm.motor.event.StopEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.DecrementCutterSpeedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.DecrementCutterSpeedEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementCutterSpeedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementCutterSpeedEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementMovementSpeedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementMovementSpeedEventHandler;
import se.purplescout.purplemow.core.fsm.mower.event.StartedMowingEvent;
import android.util.Log;

public class MotorFSM extends CoreBusSubscriberThread implements EmergencyStopEventHandler, MoveEventHandler, MowEventHandler, StopEventHandler,
		IncrementMovementSpeedEventHandler, IncrementCutterSpeedEventHandler, DecrementCutterSpeedEventHandler {

	private static final int STEP = 85;

	public enum State {
		STOPPED, MOVING_FWD, TURNING_LEFT, BACKING_UP, TURNING_RIGHT
	}

	private State state = State.STOPPED;
	private MotorController motorController;
	private int currentMovementSpeed;
	private int currentCutterSpeed;
	private CoreBus coreBus = CoreBus.getInstance();

	public MotorFSM(MotorController motorController) {
		this.motorController = motorController;
		setupSubscriptions();
	}

	private void setupSubscriptions() {
		subscribe(EmergencyStopEvent.TYPE, this);
		subscribe(MoveEvent.TYPE, this);
		subscribe(MowEvent.TYPE, this);
		subscribe(StopEvent.TYPE, this);
		subscribe(IncrementCutterSpeedEvent.TYPE, this);
		subscribe(DecrementCutterSpeedEvent.TYPE, this);
		subscribe(IncrementMovementSpeedEvent.TYPE, this);
	}

	@Override
	public void shutdown() {
		try {
			stopMotors();
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
		} finally {
			super.shutdown();
		}
	}

	@Override
	public void onStop(StopEvent event) {
		try {
			stopMotors();
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onMow(MowEvent event) {
		try {
			cutterEngine(event.getVelocity());
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onMove(MoveEvent event) {
		try {
			move(event.getDirection(), event.getVelocity());
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onEmergencyStop(EmergencyStopEvent event) {
		try {
			stopMotors();
			System.exit(1);
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onDecrementCutterSpeed(DecrementCutterSpeedEvent event) {
		try {
			int newCutterSpeed = currentCutterSpeed - STEP;
			if (newCutterSpeed < 0) {
				newCutterSpeed = 0;
			}
			cutterEngine(newCutterSpeed);
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onIncrementCutterSpeed(IncrementCutterSpeedEvent event) {
		try {
			int newCutterSpeed = currentCutterSpeed + STEP;
			if (newCutterSpeed > Constants.FULL_SPEED) {
				newCutterSpeed = Constants.FULL_SPEED;
			}
			cutterEngine(newCutterSpeed);
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onIncrementMovementSpeed(IncrementMovementSpeedEvent event) {
		try {
			int newMovementSpeed = currentMovementSpeed + STEP;
			if (newMovementSpeed > Constants.FULL_SPEED) {
				newMovementSpeed = Constants.FULL_SPEED;
			}
			if (event.getDirection() == Direction.FORWARD && state != State.MOVING_FWD) {
				newMovementSpeed = STEP;
			}
			if (event.getDirection() == Direction.BACKWARD && state != State.BACKING_UP) {
				newMovementSpeed = STEP;
			}
			if (event.getDirection() == Direction.LEFT && state != State.TURNING_LEFT) {
				newMovementSpeed = STEP;
			}
			if (event.getDirection() == Direction.RIGHT && state != State.TURNING_RIGHT) {
				newMovementSpeed = STEP;
			}
			move(event.getDirection(), newMovementSpeed);
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	private void move(Direction direction, int velocity) throws IOException {
		switch (direction) {
		case FORWARD:
			moveForward(velocity);
			coreBus.fireEvent(new StartedMowingEvent());
			break;
		case BACKWARD:
			backUp(velocity);
			break;
		case LEFT:
			turnLeft(velocity);
			break;
		case RIGHT:
			turnRight(velocity);
			break;
		default:
			break;
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
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(value, Direction.FORWARD), 500);
		}
	}

	private void backUp(int value) throws IOException {
		if (state == State.STOPPED || state == State.BACKING_UP) {
			motorController.setDirection(Direction.BACKWARD);
			move(value);
			changeState(State.BACKING_UP);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(value, Direction.BACKWARD), 500);
		}
	}

	private void turnLeft(int value) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_LEFT) {
			motorController.setDirection(Direction.LEFT);
			move(value);
			changeState(State.TURNING_LEFT);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(value, Direction.LEFT), 500);
		}
	}

	private void turnRight(int value) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_RIGHT) {
			motorController.setDirection(Direction.RIGHT);
			move(value);
			changeState(State.TURNING_RIGHT);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(value, Direction.RIGHT), 500);
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

	private void handleIOException(IOException e) {
		Log.e(getClass().getSimpleName(), getClass().getSimpleName() + " is being shutdown due to an IOException");
		Log.e(getClass().getSimpleName(), e.getMessage(), e);
		shutdown();
	}
}
