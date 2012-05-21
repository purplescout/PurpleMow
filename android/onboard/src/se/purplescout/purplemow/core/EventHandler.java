package se.purplescout.purplemow.core;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

class EventHandler extends Handler {

	private enum State {
		IDLE, MOWING, AVOID_OBSTACLE, BWF
	}

	private State state = State.IDLE;
	private MotorFSM motorFSM;
	private final TextView text;
	State oldState = null;

	protected EventHandler(MotorFSM motorFSM, TextView text) {
		this.motorFSM = motorFSM;
		this.text = text;

	}

	@Override
	public void handleMessage(Message msg) {
		if (state != oldState) {
			logToTextView("Nu jäklars! State " + state.name());
		}
		oldState = state;
		try {
			switch (state) {
			case IDLE:
				if (!motorFSM.handleMessageIdle(msg)) {
					if (msg.what == Event.START.ordinal()) {
						changeState(State.MOWING);
					}
				}
				break;
			case MOWING:
				if (!motorFSM.handleMessageMowing(msg)) {
					logToTextView("Not mowing");
					if (msg.what == ComStream.BWF_SENSOR_LEFT) {
						logToTextView("BWF_SENSOR in msg.what");
						changeState(State.BWF);
					} else if (msg.what == ComStream.RANGE_SENSOR) {
						logToTextView("DIST_SENSOR in msg.what");
						changeState(State.AVOID_OBSTACLE);
					} else {
						logToTextView("State is Mowing but no sensordata is read.");
					}
				}
				break;
			case AVOID_OBSTACLE:
				if (!motorFSM.handleMessageBwf(msg)) {
					changeState(State.MOWING);
				}
				break;
			case BWF:
				if (!motorFSM.handleMessageBwf(msg)) {
					changeState(State.MOWING);
				}
				break;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void invokeEntryAction() throws IOException {
		switch (state) {
		case IDLE:
			break;
		case MOWING:
			break;
		case AVOID_OBSTACLE:
			motorFSM.entryActionBwf();
			break;
		case BWF:
			motorFSM.entryActionBwf();
			break;
		}
	}

	private void changeState(State newState) throws IOException {
		state = newState;
		invokeEntryAction();
	}

	private void logToTextView(final String msg) {
		text.post(new Runnable() {
			@Override
			public void run() {
				CharSequence fromTextView = text.getText();
				fromTextView = msg + "\n" + fromTextView;
				text.setText(fromTextView);
			}
		});
	}
}
