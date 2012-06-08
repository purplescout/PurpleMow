package se.purplescout.purplemow.core.fsm;

import java.util.Random;

import se.purplescout.purplemow.core.Constants;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import android.util.Log;
import android.widget.TextView;

public class MainFSM extends AbstractFSM<MainFSMEvent> {

	private enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, REMOTE_CONTROLLED
	}

	private State state = State.IDLE;
	private AbstractFSM<MotorFSMEvent> motorFSM;
	private TextView textView;

	public MainFSM(TextView textView) {
		this.textView = textView;
	}

	@Override
	protected void handleEvent(MainFSMEvent event) {
		Log.v(this.getClass().getCanonicalName(), "Received event type: " + event.getEventType().toString());
		switch (state) {
		case IDLE:
			if (event.getEventType() == MainFSMEvent.EventType.STARTED_MOWING) {
				changeState(State.MOWING);
			} else if (event.getEventType() == EventType.REMOTE_CONNECTED) {
				changeState(State.REMOTE_CONTROLLED);
			}
			break;
		case MOWING:
			if (event.getEventType() == EventType.RANGE_LEFT) {
				if (event.getValue() > Constants.RANGE_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle();
				}
			} else if (event.getEventType() == EventType.RANGE_RIGHT) {
				if (event.getValue() > Constants.RANGE_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle();
				}
			} else if (event.getEventType() == EventType.BWF_RIGHT) {
				logToTextView("BWF RIGHT: " + event.getValue());
				if (event.getValue() < Constants.BWF_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle();
				}
			} else if (event.getEventType() == EventType.BWF_LEFT) {
				logToTextView("BWF LEFT: " + event.getValue());
				if (event.getValue() < Constants.BWF_LIMIT) {
					changeState(State.AVOIDING_OBSTACLE);
					avoidOstacle();
				}
			} else if (event.getEventType() == EventType.REMOTE_CONNECTED) {
				changeState(State.REMOTE_CONTROLLED);
			}
			break;
		case AVOIDING_OBSTACLE:
			if (event.getEventType() == EventType.STARTED_MOWING) {
				changeState(State.MOWING);
			}
			break;
		case REMOTE_CONTROLLED:
			if (event.getEventType() == EventType.REMOTE_DISCONNECTED) {
				changeState(State.MOWING);
			}
			break;
		}
	}

	private void avoidOstacle() {
		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP));

		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.REVERSE), 500);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP), 1500);
		if (new Random().nextBoolean()) {
			motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_LEFT), 2000);
		} else {
			motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.TURN_RIGHT), 2000);
		}
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.STOP), 3000);
		motorFSM.queueDelayedEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD), 3500);
	}

	public void setMotorFSM(AbstractFSM<MotorFSMEvent> fsm) {
		motorFSM = fsm;
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}

	private void logToTextView(final String msg) {
		Log.d(this.getClass().getCanonicalName(), msg + " " + Thread.currentThread().getId());
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
