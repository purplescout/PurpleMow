package se.purplescout.purplemow;

import java.io.IOException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

enum EventType {
	START,
	STOP,
	BWF_SENSOR,
	DIST_SENSOR,
	TIMER
}

interface Timer {
	void startTimer(int delayMillis);
}

class TmpMotorFsm {
	private enum Direction {
		FORWARD,
		BACKWARD,
		LEFT,
		RIGHT
	}
	private enum State {
	    STOPPED,
	    STOPPED_MOVE_BACKWARD,
	    STOPPED_MOVE_FORWARD,
	    STOPPED_TURN_LEFT,
	    STOPPED_TURN_RIGHT,
	    MOVING_FORWARD,
	    MOVING_BACKWARD,
	    TURNING_LEFT,
	    TURNING_RIGHT
	}
	private State state	= State.STOPPED;
	private static final int FULL_SPEED = 255;
	private static final int TOO_DARN_CLOSE = 100;
	private ComStream comStream;
	private Timer timer;

	public TmpMotorFsm(ComStream comStream, Timer timer) {
		this.comStream = comStream;
		this.timer = timer;
	}
	
	public boolean handleMessageIdle(Message msg) throws IOException {
		switch (state) {
		case STOPPED:
			if (msg.what == EventType.START.ordinal()) {
				setDirection(Direction.FORWARD);
				changeState(State.STOPPED_MOVE_FORWARD);
				timer.startTimer(200);
			}
			break;
		case STOPPED_MOVE_FORWARD:
			if (msg.what == EventType.TIMER.ordinal()) {
				move(FULL_SPEED);
				changeState(State.MOVING_FORWARD);
				comStream.readSensor();
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}

	public boolean handleMessageMowing(Message msg) throws IOException {
		switch (state) {
		case STOPPED:
			if (msg.what == EventType.DIST_SENSOR.ordinal()) {
				if (msg.arg1 <= TOO_DARN_CLOSE) {
					return true;
				} else {
					move(FULL_SPEED);
					changeState(State.MOVING_FORWARD);
				}
			} else if (msg.what == EventType.BWF_SENSOR.ordinal()) {
				Log.d(this.getClass().getName(), "BWF sensor, value = " + msg.arg1);
				if (msg.arg1 <= TOO_DARN_CLOSE) {
					return true;
				} else {
					move(FULL_SPEED);
					changeState(State.MOVING_FORWARD);
				}
			}
			break;
		case MOVING_FORWARD:
			if (msg.what == EventType.DIST_SENSOR.ordinal()) {
				if (msg.arg1 <= TOO_DARN_CLOSE) {
					move(0);
					changeState(State.STOPPED);
					return true;
				}
			} else if (msg.what == EventType.BWF_SENSOR.ordinal()) {
				Log.d(this.getClass().getName(), "BWF sensor, value = " + msg.arg1);
				if (msg.arg1 <= TOO_DARN_CLOSE) {
					move(0);
					changeState(State.STOPPED);
					return true;
				}		
			}
			comStream.readSensor();
			break;
		default:
			break;
		}		
		return false;
	}

	public boolean handleMessageAvoidingObstacle(Message msg) throws IOException {
		switch (state) {
		case STOPPED:
			setDirection(Direction.BACKWARD);
			changeState(State.STOPPED_MOVE_BACKWARD);
			timer.startTimer(200);
			break;
		case STOPPED_MOVE_BACKWARD:
			move(FULL_SPEED);
			changeState(State.MOVING_BACKWARD);
			timer.startTimer(500);
			break;
		case MOVING_BACKWARD:
			move(0);
			changeState(State.STOPPED_TURN_LEFT);
			timer.startTimer(200);
			break;
		case STOPPED_TURN_LEFT:
			setDirection(Direction.LEFT);
			move(FULL_SPEED);
			changeState(State.TURNING_LEFT);
			timer.startTimer(500);
			break;
		case TURNING_LEFT:
			move(0);
			changeState(State.STOPPED_MOVE_FORWARD);
			timer.startTimer(200);
			break;
		case STOPPED_MOVE_FORWARD:
			setDirection(Direction.FORWARD);
			changeState(State.STOPPED);
			comStream.readSensor();
			return true;
		default:
			break;
		}		
		return false;
	}

	public boolean handleMessageBwf(Message msg) throws IOException {
		switch (state) {
		case STOPPED_MOVE_BACKWARD:
			move(FULL_SPEED);
			changeState(State.MOVING_BACKWARD);
			timer.startTimer(500);
			break;
		case MOVING_BACKWARD:
			move(0);
			changeState(State.STOPPED_TURN_LEFT);
			timer.startTimer(200);
			break;
		case STOPPED_TURN_LEFT:
			setDirection(Direction.LEFT);
			move(FULL_SPEED);
			changeState(State.TURNING_LEFT);
			timer.startTimer(500);
			break;
		case TURNING_LEFT:
			move(0);
			changeState(State.STOPPED_MOVE_FORWARD);
			timer.startTimer(200);
			break;
		case STOPPED_MOVE_FORWARD:
			setDirection(Direction.FORWARD);
			changeState(State.STOPPED);
			comStream.readSensor();
			return true;
		default:
			break;
		}		
		return false;
	}

	public void entryActionBwf() throws IOException {
		switch (state) {
		case STOPPED:
			setDirection(Direction.BACKWARD);
			changeState(State.STOPPED_MOVE_BACKWARD);
			timer.startTimer(200);
			break;
		}
	}
	
		
	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}
	
	
	private void move(int speed) throws IOException {
		comStream.sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO1, speed);
		comStream.sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO2, speed);
	}

	private void setDirection(Direction direction) throws IOException {
		switch (direction) {
		case FORWARD:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
			break;
		case BACKWARD:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
		case LEFT:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
		case RIGHT:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
		}
	}

}

class EventHandler extends Handler {
	private enum State {
		IDLE, MOWING, AVOID_OBSTACLE, BWF
	}

	private State state	= State.IDLE;
	private TmpMotorFsm motorFsm;
	
	EventHandler(TmpMotorFsm motorFsm) {
		this.motorFsm = motorFsm;
	}

	public void handleMessage(Message msg) {
//		Log.d(this.getClass().getName(), "handleMessage, msg = " + msg.what + ", state = " + state);
		
		try {
			switch (state) {
			case IDLE:
				if (motorFsm.handleMessageIdle(msg)) {
					changeState(State.MOWING);
				}
				break;
			case MOWING:
				if (motorFsm.handleMessageMowing(msg)) {
					if (msg.what == EventType.DIST_SENSOR.ordinal()) {
						changeState(State.AVOID_OBSTACLE);
					} else if (msg.what == EventType.BWF_SENSOR.ordinal()) {
						changeState(State.BWF);
					}
				}
				break;
			case AVOID_OBSTACLE:
				if (motorFsm.handleMessageAvoidingObstacle(msg)) {
					changeState(State.MOWING);
				}
				break;
			case BWF:
				if (motorFsm.handleMessageBwf(msg)) {
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
			break;
		case BWF:
			motorFsm.entryActionBwf();
			break;
		}
	}

	private void changeState(State newState) throws IOException {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
		invokeEntryAction();
	}
}


public class MainFsm implements Runnable, Timer {
	private SensorReader sr;
	private TmpMotorFsm motorFsm;
	private Handler handler;
	
	public MainFsm(ComStream comStream) {
		this.sr = new SensorReader(comStream);
		this.motorFsm = new TmpMotorFsm(comStream, this);
	}

	public void start() {
		Thread thread = new Thread(null, this, "PurpleMow");
		thread.start();
	}
	
	@Override
	public void run() {
		Looper.prepare();
		
		handler = new EventHandler(motorFsm);
		handler.sendEmptyMessage(EventType.START.ordinal());

		sr.connect(handler);
		sr.start();
		
		Looper.loop();
	}

	@Override
	public void startTimer(int delayMillis) {
		Log.d(this.getClass().getName(), "Start timer event: " + delayMillis + " ms");
		handler.sendEmptyMessageDelayed(EventType.TIMER.ordinal(), delayMillis);
	}
}
