package se.purplescout.purplemow.core.fsm.event;

public class MainFSMEvent implements Comparable<MainFSMEvent> {
	public enum EventType {
		BWF_LEFT(5), BWF_RIGHT(5), RANGE(4), AVOIDING_OBSTACLE_DONE(6);
		
		private int priority;

		private EventType(int priority) {
			this.priority = priority;
		}
	}
	
	private final EventType eventType;
	private final int value;
	
	public MainFSMEvent(EventType eventType) {
		this(eventType, -1);
	}

	public MainFSMEvent(EventType eventType, int value) {
		this.eventType = eventType;
		this.value = value;
	}

	public EventType getEventType() {
		return eventType;
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public int compareTo(MainFSMEvent another) {
		return Integer.signum(eventType.priority - another.getEventType().priority);
	}
}
	