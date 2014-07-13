package se.purplescout.purplemow.core.fsm.motor;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.MotorController;
import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriberThread;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEvent;
import se.purplescout.purplemow.core.fsm.common.event.NewConstantsEventHandler;
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
		IncrementMovementSpeedEventHandler, IncrementCutterSpeedEventHandler, DecrementCutterSpeedEventHandler, NewConstantsEventHandler {

	private static final int STEP = 85;

	public enum State {
		STOPPED, MOVING_FWD, TURNING_LEFT, BACKING_UP, TURNING_RIGHT
	}

	private State state = State.STOPPED;
	private MotorController motorController;
	private int currentMovementSpeed;
	private int currentCutterSpeed;
	private CoreBus coreBus = CoreBus.getInstance();
	private Constants constants;

	public MotorFSM(Constants constants, ComStream comStream) {
		this.constants = constants;
		this.motorController = new MotorController(comStream, constants);
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
			move(event.getDirection(), event.getSpeedRight(), event.getSpeedLeft());
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onEmergencyStop(EmergencyStopEvent event) {
		Log.e(this.getClass().getSimpleName(), "Emergency stop received in MotorFSM. Shutting down.");
		try {
			stopMotors();
			cutterEngine(0);
//			System.exit(1);
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
			if (newCutterSpeed > constants.getFullSpeed()) {
				newCutterSpeed = constants.getFullSpeed();
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
			if (newMovementSpeed > constants.getFullSpeed()) {
				newMovementSpeed = constants.getFullSpeed();
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
			move(event.getDirection(), newMovementSpeed, newMovementSpeed);
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	public void onNewConstants(NewConstantsEvent event) {
		this.constants = event.getConstants();
		motorController.updateConstants(constants);
	}

	private void move(Direction direction, int speedRight, int speedLeft) throws IOException {
		switch (direction) {
		case FORWARD:
			moveForward(speedRight, speedLeft);
			coreBus.fireEvent(new StartedMowingEvent());
			break;
		case BACKWARD:
			backUp(speedRight, speedLeft);
			break;
		case LEFT:
			turnLeft(speedRight, speedLeft);
			break;
		case RIGHT:
			turnRight(speedRight, speedLeft);
			break;
		default:
			break;
		}
	}

	private void stopMotors() throws IOException {
		Log.v(getClass().getSimpleName(), getClass().getSimpleName() + ": stopping motors.");
		motorController.move(0);
		changeState(State.STOPPED);
	}

	private void moveForward(int valueR, int valueL) throws IOException {
		if (state == State.STOPPED || state == State.MOVING_FWD) {
			motorController.setDirection(Direction.FORWARD);
			move(valueR, valueL);
			changeState(State.MOVING_FWD);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(valueR, valueL, Direction.FORWARD), 500);
		}
	}

	private void backUp(int valueR, int valueL) throws IOException {
		if (state == State.STOPPED || state == State.BACKING_UP) {
			motorController.setDirection(Direction.BACKWARD);
			move(valueR, valueL);
			changeState(State.BACKING_UP);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(valueR, valueL, Direction.BACKWARD), 500);
		}
	}

	private void turnLeft(int valueR, int valueL) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_LEFT) {
			motorController.setDirection(Direction.LEFT);
			move(valueR, valueL);
			changeState(State.TURNING_LEFT);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(valueR, valueL, Direction.LEFT), 300);
		}
	}

	private void turnRight(int valueR, int valueL) throws IOException {
		if (state == State.STOPPED || state == State.TURNING_RIGHT) {
			motorController.setDirection(Direction.RIGHT);
			move(valueR, valueL);
			changeState(State.TURNING_RIGHT);
		} else {
			coreBus.fireEvent(new StopEvent());
			coreBus.fireDelaydEvent(new MoveEvent(valueR, valueL, Direction.RIGHT), 500);
		}
	}

	private void move(int valueR, int valueL) throws IOException {
		motorController.move(valueR, valueL);
		currentMovementSpeed = Math.max(valueL, valueR);
	}

	private void cutterEngine(int value) throws IOException {
		Log.v(getClass().getSimpleName(), getClass().getSimpleName() + ": Setting speed on cutter motor to " + value);
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
