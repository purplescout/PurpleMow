package se.purplescout.purplemow.core.fsm;

import java.util.Timer;
import java.util.concurrent.PriorityBlockingQueue;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.fsm.event.Event;
import se.purplescout.purplemow.core.fsm.event.EventComparator;
import se.purplescout.purplemow.core.fsm.event.EventType;
import se.purplescout.purplemow.core.sensor.SensorReader;
import android.util.Log;
import android.widget.TextView;

public class MainFSM implements Runnable {

	public enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, BWF
	}

	private State state;
	private SensorReader sensorReader;
	private MotorFSM motorFSM;
	private Thread main;
	private PriorityBlockingQueue<Event> eventQueue;
	private PriorityBlockingQueue<Event> motorFSMQueue;
	private ComStream comStream;
	private boolean isRunning = false;
	
	TextView text;

	public MainFSM(ComStream comStream, TextView text) {
		state = State.IDLE;
		eventQueue = new PriorityBlockingQueue<Event>(5, new EventComparator());
		motorFSMQueue = new PriorityBlockingQueue<Event>(5, new EventComparator());
		this.comStream = comStream;
		this.text = text;
	}

	public void start() {
		isRunning = true;
		sensorReader = new SensorReader(comStream);
		
		motorFSM = new MotorFSM(comStream, eventQueue, motorFSMQueue);
		main = new Thread(null, this, "PurpleMow");
		Timer sensorReaderTimer = new Timer();
		sensorReaderTimer.schedule(sensorReader, 100, 100);
//		motorFSM.start();
		main.start();
		eventQueue.put(new Event(EventType.START));
		Log.e(this.getClass().getName(), "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#########################################!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	
	public void stop() {
		isRunning = false;
		sensorReader.cancel();
		motorFSM.cancel();
	}

	@Override
	public void run() {
		while (isRunning) {
			try {
				Event event;
				Log.i(this.getClass().getName(), "Entering " + state + " state");
				switch (state) {
				case IDLE:
					int val = sensorReader.getLatestDistanceValue();
					Log.i(this.getClass().getName(), "Current Distance: " + Integer.toString(val));
//					event = eventQueue.take();
//					if (event.type == EventType.START) {
//						motorFSMQueue.add(new Event(EventType.MOVE_FORWARD));
//						Log.i(this.getClass().getName(), "Enter MOWING state");
//						changeState(State.MOWING);
//					}
					break;
				case MOWING:
					val = sensorReader.getLatestDistanceValue();
					Log.i(this.getClass().getName(), Integer.toString(val));
					if (val < 100) {
						motorFSMQueue.add(new Event(EventType.AVOID_OBSTACLE_LEFT));
						Log.i(this.getClass().getName(), "Enter AVOIDING_OBSTACLE state");
						changeState(State.AVOIDING_OBSTACLE);
					}
					break;
				case AVOIDING_OBSTACLE:
					event = eventQueue.take();
					if (event.type == EventType.AVOIDING_OBSTACLE_DONE) {
						motorFSMQueue.add(new Event(EventType.MOVE_FORWARD));
						Log.i(this.getClass().getName(), "Enter MOWING state");
						changeState(State.MOWING);
					}
					break;
				default:
					;
				}
			} catch (InterruptedException e) {
				Log.e(this.getClass().getName(), e.getMessage());
				e.printStackTrace();
			}
			//TODO ta bort
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}
}
