package se.purplescout.purplemow.core.fsm;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.MotorController;
import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent.EventType;
import se.purplescout.purplemow.onboard.GuiLogCallback;
import android.util.Log;

public class MotorFSM extends AbstractFSM<MotorFSMEvent> {

	private enum State {
		STOPPED, MOVING
	}

	private State state = State.STOPPED;
	private MotorController motorController;
	private final GuiLogCallback logCallback;
	private AbstractFSM<MainFSMEvent> mainFSM;

	@Override
	public void shutdown() {
		try {
			stopMotors();
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
		} finally {
			super.shutdown();
		}
	}

	public MotorFSM(ComStream comStream, GuiLogCallback logCallback) {
		this.logCallback = logCallback;
		this.motorController = new MotorController(comStream);
	}
	
	public void setMainFSM(AbstractFSM<MainFSMEvent> fsm) {
		this.mainFSM = fsm;
	}
	
	@Override
	protected void handleEvent(MotorFSMEvent event)  {
		logToTextView(event.getEventType().name());
		try {
			switch (event.getEventType()) {
			case MOVE_FWD:
				moveForward();
				mainFSM.queueEvent(new MainFSMEvent(MainFSMEvent.EventType.STARTED_MOWING));
				break;
			case REVERSE:
				backUp();
				break;
			case TURN_LEFT:
				turnLeft();
				break;
			case TURN_RIGHT:
				turnRight();
				break;
			case STOP:
				stopMotors();
				break;
			case EMERGENCY_STOP:
				stopMotors();
				System.exit(1);
				break;			
			default:
				break;
			}
		} catch (IOException e) {
			logToTextView(e.getMessage());
		}
	}

	private void stopMotors() throws IOException {
		motorController.move(0);
		changeState(State.STOPPED);
	}

	private void moveForward() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.FORWARD);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.MOVE_FWD), 500);
		}
	}
	
	private void backUp() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.BACKWARD);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.REVERSE), 500);
		}
	}
	
	private void turnLeft() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.LEFT);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_LEFT), 500);
		}
	}
	
	private void turnRight() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.RIGHT);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_RIGHT), 500);
		}
	}

	private void changeState(State newState) {
		String aMessage = "Change state from " + state + ", to " + newState;
		logToTextView(aMessage);
		
		state = newState;
	}

	private void logToTextView(final String msg) {
		Log.d(this.getClass().getName(), msg + " " + Thread.currentThread().getId());
		logCallback.post(msg);
	}

}
