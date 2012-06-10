package se.purplescout.purplemow.core.fsm.event;

public class MotorFSMEvent implements Comparable<MotorFSMEvent>{
	public enum EventType {
		EMERGENCY_STOP(10), MOVE_FWD(5), REVERSE(5), TURN_LEFT(5), TURN_RIGHT(5), STOP(5);
		
		private int priority;

		private EventType(int priority) {
			this.priority = priority;
		}
	}
	
	private final EventType eventType;
	private final int value;
	
	public MotorFSMEvent(EventType eventType) {
		this(eventType, -1);
	}

	public MotorFSMEvent(EventType eventType, int value) {
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
	public int compareTo(MotorFSMEvent another) {
		return Integer.signum(eventType.priority - another.getEventType().priority);
	}
}
