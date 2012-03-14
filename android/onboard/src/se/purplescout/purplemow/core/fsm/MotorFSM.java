package se.purplescout.purplemow.core.fsm;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.fsm.event.Event;
import se.purplescout.purplemow.core.fsm.event.EventType;
import se.purplescout.purplemow.core.motor.MotorController;
import android.util.Log;

public class MotorFSM implements Runnable {

	public enum State {
		STILL, MOVING_FORWARD, MOVING_BACKWARD, TURNING_LEFT, TURNING_RIGHT
	}

	private State state;
	private PriorityBlockingQueue<Event> motorFSMQueue;
	private PriorityBlockingQueue<Event> mainFSMQueue;
	private MotorController motorController;
	private boolean isRunning;

	public MotorFSM(ComStream comStream, PriorityBlockingQueue<Event> mainFSMQueue, PriorityBlockingQueue<Event> motorFSMQueue) {
		this.motorController = new MotorController(comStream);
		this.mainFSMQueue = mainFSMQueue;
		this.motorFSMQueue = motorFSMQueue;
	}
	
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

	//TODO Observera att alla anrop till motorn sker i denna tråd och således låser MotorFSM. Så borde ej ske.
	private void handleEvent(Event event) {
		try{
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
		Thread.sleep(2000);
		motorController.turnLeft(state);
		changeState(State.TURNING_LEFT);
		Thread.sleep(2000);
		motorController.stop();
		changeState(State.STILL);
		mainFSMQueue.add(new Event(EventType.AVOIDING_OBSTACLE_DONE));
	}
	
	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}
}
