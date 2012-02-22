package se.purplescout.purplemow;

import java.util.Comparator;
import java.util.PriorityQueue;

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
	private PriorityQueue<Event> eventQueue;

	public MainFsm(ComStream comStream) {
		this.sr = new SensorReader(comStream);
		this.motorFsm = new MotorFsm(comStream, sr);
		eventQueue = new PriorityQueue<MainFsm.Event>(5, new EventComparator());
		state = State.IDLE;
	}

	public void start() {
		eventQueue.add(new Event(EventType.START));
	}

	@Override
	public void run() {
		Event e = eventQueue.poll();
		switch (state) {
		case IDLE:
			if (e != null && e.type == EventType.START) {
				state = State.MOWING;
			}
			break;
		case MOWING:
			motorFsm.run();
			break;
		case AVOID_OBSTACLE:
			break;
		case BWF:
			break;
		}
	}

}
