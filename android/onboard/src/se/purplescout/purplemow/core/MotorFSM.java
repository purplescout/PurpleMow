package se.purplescout.purplemow.core;

import static se.purplescout.purplemow.core.Constants.*;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import android.os.Message;
import android.util.Log;

public class MotorFSM {

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
	private SensorReader sensorReader;
	private Timer timer;
	private MotorController motorController;
	private boolean isRunning;


	public MotorFSM(SensorReader sensorReader, ComStream comStream, Timer timer) {
		this.sensorReader = sensorReader;
		this.motorController = new MotorController(comStream);
		this.timer = timer;
	}

	public boolean handleMessageIdle(Message msg) throws IOException {
		if (msg.what == Event.START.ordinal()) {
			sensorReader.requestSensor(SENSOR_BWF);
		}
		return false;
	}

	/**
	 * Handles events in the Mowing state
	 * @param msg
	 * @return true if event is handled, false otherwise
	 * @throws IOException
	 */
	public boolean handleMessageMowing(Message msg) throws IOException {
		switch (state) {
		case STOPPED:
			motorController.setDirection(Direction.FORWARD);
			changeState(State.STOPPED_MOVE_FORWARD);
			timer.startTimer(100);
			return true;
		case STOPPED_MOVE_FORWARD:
			if (msg.what == Event.TIMER.ordinal()) {
				sensorReader.requestSensor(SENSOR_BWF);
				return true;
			} else if (msg.what == Event.BWF_SENSOR.ordinal()) {
				Log.d(this.getClass().getName(), "BWF sensor, value = " + msg.arg1);
				if (msg.arg1 > TOO_DARN_CLOSE) {
					motorController.move(FULL_SPEED);
					changeState(State.MOVING_FORWARD);
					sensorReader.requestSensor(SENSOR_BWF);
					return true;
				}
			}
			break;
		case MOVING_FORWARD:
			if (msg.what == Event.BWF_SENSOR.ordinal()) {
				Log.d(this.getClass().getName(), "BWF sensor, value = " + msg.arg1);
				if (msg.arg1 <= TOO_DARN_CLOSE) {
					motorController.move(0);
					changeState(State.STOPPED_MOVE_FORWARD);
				} else {
					sensorReader.requestSensor(SENSOR_BWF);
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
		switch (state) {
		case STOPPED_MOVE_BACKWARD:
			motorController.move(FULL_SPEED);
			changeState(State.MOVING_BACKWARD);
			timer.startTimer(500);
			return true;
		case MOVING_BACKWARD:
			motorController.move(0);
			changeState(State.STOPPED_TURN_LEFT);
			timer.startTimer(200);
			return true;
		case STOPPED_TURN_LEFT:
			motorController.setDirection(Direction.LEFT);
			motorController.move(FULL_SPEED);
			changeState(State.TURNING_LEFT);
			timer.startTimer(500);
			return true;
		case TURNING_LEFT:
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
			assert(false);
		}
		return false;
	}

	public void entryActionBwf() throws IOException {
		switch (state) {
		case STOPPED:
		case STOPPED_MOVE_FORWARD:
		case STOPPED_TURN_LEFT:
		case STOPPED_TURN_RIGHT:
			motorController.setDirection(Direction.BACKWARD);
			changeState(State.STOPPED_MOVE_BACKWARD);
			timer.startTimer(200);
			break;
		default:
			assert(false);
		}
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}

	/*
	public void start() {
		isRunning = true;
		state = State.STILL;
		new Thread(this).start();
	}

	@Override
	public void run() {
		while (isRunning) {
			try {
				Event event;
				switch (state) {
				case STILL:
					event = motorFSMQueue.take();
					if (event.type != EventType.STOP) {
						handleEvent(event);
					}
					break;
				case MOVING_FORWARD:
					event = motorFSMQueue.take();
					if (event.type != EventType.MOVE_FORWARD) {
						handleEvent(event);
					}
					break;
				case MOVING_BACKWARD:
					event = motorFSMQueue.take();
					if (event.type != EventType.MOVE_BACKWARD) {
						handleEvent(event);
					}
					break;
				case TURNING_LEFT:
					event = motorFSMQueue.take();
					if (event.type != EventType.TURN_LEFT) {
						handleEvent(event);
					}
					break;
				case TURNING_RIGHT:
					event = motorFSMQueue.take();
					if (event.type != EventType.TURN_RIGHT) {
						handleEvent(event);
					}
					break;
				default:
					;
				}
			} catch (InterruptedException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// TODO Observera att alla anrop till motorn sker i denna tråd och således låser MotorFSM. Så borde ej ske.
	private void handleEvent(Event event) {
		try {
			switch (event.type) {
			case MOVE_FORWARD:
				moveForward();
				changeState(State.MOVING_FORWARD);
				break;
			case MOVE_BACKWARD:
				moveBackward();
				changeState(State.MOVING_BACKWARD);
				break;
			case TURN_LEFT:
				turnLeft();
				changeState(State.TURNING_LEFT);
				break;
			case TURN_RIGHT:
				turnRight();
				changeState(State.TURNING_RIGHT);
				break;
			case AVOID_OBSTACLE_LEFT:
				avoidObstacleLeft();
				break;
			case AVOID_OBSTACLE_RIGHT:
				avoidObstacleRight();
				break;
			default:
				break;
			}
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(this.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	public void cancel() {
		isRunning = false;
	}

	private void moveForward() throws IOException {
		motorController.move(state, 1);
	}

	private void moveBackward() throws IOException {
		motorController.move(state, 1);
	}

	private void turnLeft() throws IOException {
		motorController.turnLeft(state);
	}

	private void turnRight() throws IOException {
		motorController.turnLeft(state);
	}

	private void avoidObstacleLeft() throws IOException, InterruptedException {
		motorController.moveBackward(state);
		changeState(State.MOVING_BACKWARD);
		Thread.sleep(800);
		motorController.turnRight(state);
		changeState(State.TURNING_RIGHT);
		Thread.sleep(600);
		motorController.stop();
		changeState(State.STILL);
		mainFSMQueue.add(new Event(EventType.AVOIDING_OBSTACLE_DONE));
	}

	private void avoidObstacleRight() throws IOException, InterruptedException {
		motorController.moveBackward(state);
		changeState(State.MOVING_BACKWARD);
		Thread.sleep(800);
		motorController.turnLeft(state);
		changeState(State.TURNING_LEFT);
		Thread.sleep(600);
		motorController.stop();
		changeState(State.STILL);
		mainFSMQueue.add(new Event(EventType.AVOIDING_OBSTACLE_DONE));
	}
*/
}
