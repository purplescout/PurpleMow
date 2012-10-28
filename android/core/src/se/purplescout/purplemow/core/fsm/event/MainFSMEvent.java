package se.purplescout.purplemow.core.fsm.event;

public class MainFSMEvent extends FSMEvent<MainFSMEvent.EventType, MainFSMEvent> {
	public enum EventType {
		BWF_LEFT(5), BWF_RIGHT(5), RANGE_LEFT(5), RANGE_RIGHT(5), STARTED_MOWING(5), REMOTE_CONNECTED(5), REMOTE_DISCONNECTED(5);

		private int priority;

		private EventType(int priority) {
			this.priority = priority;
		}
	}

	public MainFSMEvent(EventType eventType) {
		super(eventType);
	}

	public MainFSMEvent(EventType eventType, int value) {
		super(eventType, value);
	}

	@Override
	public int compareTo(MainFSMEvent another) {
		return Integer.signum(eventType.priority - another.getEventType().priority);
	}

	@Override
	public String toString() {
		return "MainFSMEvent [eventType=" + eventType + ", value=" + value + "]";
	}
}
