package se.purplescout.purplemow.core.fsm;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.Constants.Direction;
import se.purplescout.purplemow.core.MotorController;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent.EventType;
import android.util.Log;
import android.widget.TextView;

public class MotorFSM extends AbstractFSM<MotorFSMEvent> {

	private enum State {
		STOPPED, MOVING_BACKWARD, TURNING_LEFT, TURNING_RIGHT, MOVING_FORWARD
	}

	private State state = State.STOPPED;
	
	private MotorController motorController;
	private final TextView textView;
	private AbstractFSM<MainFSMEvent> mainFSM;

	public MotorFSM(ComStream comStream, TextView textView) {
		this.textView = textView;
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
				motorController.move(0);
				changeState(State.STOPPED);
				break;
			case EMERGENCY_STOP:
				motorController.move(0);
				System.exit(1);
				break;			
			default:
				break;
			}
		} catch (IOException e) {
			logToTextView(e.getMessage());
		}
	}

	private void moveForward() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.FORWARD);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING_FORWARD);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.MOVE_FWD), 500);
		}
	}
	
	private void backUp() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.BACKWARD);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.MOVING_BACKWARD);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.REVERSE), 500);
		}
	}
	
	private void turnLeft() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.LEFT);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.TURNING_LEFT);
		} else {
			queueEvent(new MotorFSMEvent(EventType.STOP));
			queueDelayedEvent(new MotorFSMEvent(EventType.TURN_LEFT), 500);
		}
	}
	
	private void turnRight() throws IOException {
		if (state == State.STOPPED) {
			motorController.setDirection(Direction.RIGHT);
			motorController.move(Constants.FULL_SPEED);
			changeState(State.TURNING_RIGHT);
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
		textView.post(new Runnable() {

			@Override
			public void run() {
				textView.append(msg + "\n");
				CharSequence fromTextView = textView.getText();
				fromTextView = msg + "\n" + fromTextView;
				textView.setText(fromTextView);
			}
		});
	}

}
