package se.purplescout.purplemow;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import android.util.Log;

public class MainFsm implements Runnable {

	public enum State {
		IDLE, MOWING, AVOID_OBSTACLE, BWF
	}

	public enum EventType {
		START
	}

	public enum EventPriority {
		HIGH, DEFAULT, LOW
	}

	class Event {
		EventType type;
		EventPriority prio;

		Event(EventType type, EventPriority prio) {
			this.type = type;
			this.prio = prio;
		}

		Event(EventType type) {
			this.type = type;
			this.prio = EventPriority.DEFAULT;
		}
	}

	class EventComparator implements Comparator<Event> {
		@Override
		public int compare(Event left, Event right) {
			return left.prio.compareTo(right.prio);
		}

	}

	private State state;
	private SensorReader sr;
	private MotorFsm motorFsm;
	private PriorityBlockingQueue<Event> eventQueue;

	public MainFsm(ComStream comStream) {
		this.sr = new SensorReader(comStream);
		this.motorFsm = new MotorFsm(comStream, sr);
		eventQueue = new PriorityBlockingQueue<MainFsm.Event>(5, new EventComparator());
		state = State.IDLE;
	}

	public void start() {
		Thread thread = new Thread(null, this, "PurpleMow");
		thread.start();	
		
		eventQueue.put(new Event(EventType.START));
	}

	@Override
	public void run() {
		Event ev;

		while (true) {
			
			try {
				switch (state) {
				case IDLE:
					ev = eventQueue.take();
					if (ev.type == EventType.START) {
						changeState(State.MOWING);
					}
					break;
				case MOWING:
					break;
				case AVOID_OBSTACLE:
					break;
				case BWF:
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
		handleNewStateEntry();
	}

	private void handleNewStateEntry() {
		switch (state) {
		case IDLE:
			break;
		case MOWING:
			motorFsm.startMowing();
			break;
		case AVOID_OBSTACLE:
			break;
		case BWF:
			break;
		}
	}
	
	

}
