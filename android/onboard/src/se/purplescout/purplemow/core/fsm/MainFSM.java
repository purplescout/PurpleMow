package se.purplescout.purplemow.core.fsm;

import java.util.Random;
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
		IDLE, MOWING, AVOIDING_OBSTACLE, BWF, UNKNOWN
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
		motorFSM.start();
		main.start();
		eventQueue.put(new Event(EventType.START));
		// To find the start of the application in the log
		Log.e(this.getClass().getName(),
				"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!####################################################################!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	public void stop() {
		isRunning = false;
		sensorReader.cancel();
		motorFSM.cancel();
	}

	@Override
	public void run() {
		State oldState = State.UNKNOWN;
		int oldDistance = 9999;
		Random randoimizer = new Random();
		while (isRunning) {
			try {
				Event event;

				// Debug-printa lite dynga om avstånd eller status har ändrats
				if (!state.equals(oldState)) {
					logToTextView("State is " + state);
				}
				oldState = state;

				// Printa bara om diffen är tillräckligt stor
				int diff = oldDistance - sensorReader.getLatestDistanceValue();
				if (Math.abs(diff) > 10) {
					String msg = String.format("Avstånd %d", sensorReader.getLatestDistanceValue());
					logToTextView(msg);
					oldDistance = sensorReader.getLatestDistanceValue();
				}
				switch (state) {
				case IDLE:
					// logToTextView("Doing IDLE");
					event = eventQueue.take();
					if (event.type == EventType.START) {
						motorFSMQueue.add(new Event(EventType.MOVE_FORWARD));
						Log.i(this.getClass().getName(), "Enter MOWING state");
						changeState(State.MOWING);
					}
					break;
				case MOWING:
					// logToTextView("Doing MOWING");
					int val = sensorReader.getLatestDistanceValue();
					if (val > 420) {
						// Avoid left or right? Dunno yet since there is only one sensor. Do it randomly for now.
						boolean right = randoimizer.nextBoolean();
						if (right) {
							logToTextView("Avoiding obstacle right");
							motorFSMQueue.add(new Event(EventType.AVOID_OBSTACLE_RIGHT));
						} else {
							logToTextView("Avoiding obstacle left");
							motorFSMQueue.add(new Event(EventType.AVOID_OBSTACLE_LEFT));
						}
						Log.i(this.getClass().getName(), "Enter AVOIDING_OBSTACLE state");
						changeState(State.AVOIDING_OBSTACLE);
					}
					break;
				case AVOIDING_OBSTACLE:
					// logToTextView("Doing AVOID_OBSTACLE");
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

		}
	}

	private void logToTextView(final String msg) {

		text.post(new Runnable() {
			@Override
			public void run() {
				text.append(msg + "\n");
			}
		});
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}
}
