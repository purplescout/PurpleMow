package se.purplescout.purplemow.core;

import static se.purplescout.purplemow.core.Constants.FULL_SPEED;
import static se.purplescout.purplemow.core.Constants.SENSOR_BWF;
import static se.purplescout.purplemow.core.Constants.TOO_DARN_CLOSE;

import java.io.IOException;
import java.util.Random;

import se.purplescout.purplemow.core.Constants.Direction;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class MotorFSM {

	private enum State {
		STOPPED, STOPPED_MOVE_BACKWARD, STOPPED_MOVE_FORWARD, STOPPED_TURN_LEFT, STOPPED_TURN_RIGHT, MOVING_FORWARD, MOVING_BACKWARD, TURNING_LEFT, TURNING_RIGHT, STOPPED_SETTING_DIRECTION, TURNING
	}

	private State state = State.STOPPED;
	private SensorReader sensorReader;
	private Timer timer;
	private MotorController motorController;
	private boolean isRunning;
	private final TextView textView;
	private Random randoimizer = new Random();

	public MotorFSM(SensorReader sensorReader, ComStream comStream, Timer timer, TextView textView) {
		this.sensorReader = sensorReader;
		this.textView = textView;
		this.motorController = new MotorController(comStream);
		this.timer = timer;
	}

	public boolean handleMessageIdle(Message msg) throws IOException {
		if (msg.what == Event.START.ordinal()) {
			sensorReader.requestSensor(ComStream.RANGE_SENSOR);
		}
		return false;
	}

	/**
	 * Handles events in the Mowing state
	 * 
	 * @param msg
	 * @return true if event is handled, false otherwise
	 * @throws IOException
	 */
	public boolean handleMessageMowing(Message msg) throws IOException {
		logToTextView("Inside handleMessageMowing");
		switch (state) {
		case STOPPED:
			logToTextView("STOPPED: " + msg.arg1 + ": " + msg.what);
			motorController.setDirection(Direction.FORWARD);
			changeState(State.STOPPED_MOVE_FORWARD);
			timer.startTimer(100);
			return true;
		case STOPPED_MOVE_FORWARD:
			logToTextView("ST_MO_FWD: " + msg.arg1 + ": " + msg.what);
			if (msg.what == Event.TIMER.ordinal()) {
				logToTextView("Timer event received. ");
				sensorReader.requestSensor(ComStream.RANGE_SENSOR);
				return true;
			} else if (msg.what == ComStream.RANGE_SENSOR) {
				logToTextView("ST_MO_FWD, bwf sensor detected: " + msg.arg1 + ": " + msg.what);
				Log.d(this.getClass().getName(), "Range sensor, value = " + msg.arg1);
				if (msg.arg1 < TOO_DARN_CLOSE) {
					motorController.move(FULL_SPEED);
					changeState(State.MOVING_FORWARD);
					sensorReader.requestSensor(ComStream.RANGE_SENSOR);
					return true;
				}
			}
			logToTextView("Varför hamnade vi här?");
			break;
		case MOVING_FORWARD:
			logToTextView("MOVING_FWD: " + msg.arg1 + ": " + msg.what);
			// if (msg.what == Event.BWF_SENSOR.ordinal()) {
			if (msg.what == ComStream.RANGE_SENSOR) {
				logToTextView("Range sensor read.");
				Log.d(this.getClass().getName(), "Range sensor, value = " + msg.arg1);
				if (msg.arg1 >= TOO_DARN_CLOSE) {
					logToTextView("Close to obstacle. Avoid!!!!");
					motorController.move(0);
					changeState(State.STOPPED_MOVE_FORWARD);
				} else {
					sensorReader.requestSensor(ComStream.RANGE_SENSOR);
					return true;
				}
			}
			break;
		default:
			break;
		}
		return false;
	}

	public boolean handleMessageBwf(Message msg) throws IOException {
		logToTextView("Inside handleBWF");
		switch (state) {
		case STOPPED_MOVE_BACKWARD:
			motorController.move(FULL_SPEED);
			changeState(State.MOVING_BACKWARD);
			// Move backwards this amount of time
			timer.startTimer(1000);
			return true;
		case MOVING_BACKWARD:
			motorController.move(0);
			changeState(State.STOPPED_TURN_LEFT);
			timer.startTimer(200);
			return true;
		case STOPPED_TURN_LEFT:
			boolean right = randoimizer.nextBoolean();
			if (right) {
				motorController.setDirection(Direction.RIGHT);
			} else {
				motorController.setDirection(Direction.LEFT);
			}
			changeState(State.STOPPED_SETTING_DIRECTION);
			timer.startTimer(200);
			return true;
		case STOPPED_SETTING_DIRECTION:
			motorController.move(FULL_SPEED);
			changeState(State.TURNING);
			timer.startTimer(500);
			return true;
		case TURNING:
			motorController.move(0);
			changeState(State.STOPPED_MOVE_FORWARD);
			timer.startTimer(200);
			return true;
		case STOPPED_MOVE_FORWARD:
			motorController.setDirection(Direction.FORWARD);
			changeState(State.STOPPED);
			sensorReader.requestSensor(SENSOR_BWF);
			return false;
		default:
			assert (false);
		}
		return false;
	}

	public void entryActionBwf() throws IOException {
		logToTextView("Inside entryActionBWF");
		switch (state) {
		case STOPPED:
		case STOPPED_MOVE_FORWARD:
		case STOPPED_TURN_LEFT:
		case STOPPED_TURN_RIGHT:
			motorController.setDirection(Direction.BACKWARD);
			changeState(State.STOPPED_MOVE_BACKWARD);
			timer.startTimer(500);
			break;
		default:
			assert (false);
		}
	}

	private void changeState(State newState) {
		String aMessage = "Change state from " + state + ", to " + newState;
		Log.d(this.getClass().getName(), aMessage);
		state = newState;
		logToTextView("Change state from " + state + ", to " + newState);

	}

	private void logToTextView(final String msg) {
		textView.post(new Runnable() {

			@Override
			public void run() {
				textView.append(msg + "\n");
			}
		});
	}
}
