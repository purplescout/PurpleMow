package se.purplescout.purplemow.core.fsm.event;

public class MotorFSMEvent extends FSMEvent<MotorFSMEvent.EventType, MotorFSMEvent> {
	public enum EventType {
		EMERGENCY_STOP(10), MOVE_FWD(5), REVERSE(5), TURN_LEFT(5), TURN_RIGHT(5), STOP(5), MOW(5);

		private int priority;

		private EventType(int priority) {
			this.priority = priority;
		}
	}
	
	public MotorFSMEvent(EventType eventType) {
		super(eventType);
	}

	public MotorFSMEvent(EventType eventType, int value) {
		super(eventType, value);
	}

	@Override
	public int compareTo(MotorFSMEvent another) {
		return Integer.signum(eventType.priority - another.getEventType().priority);
	}

	@Override
	public String toString() {
		return "MotorFSMEvent [eventType=" + eventType + ", value=" + value + "]";
	}
}
