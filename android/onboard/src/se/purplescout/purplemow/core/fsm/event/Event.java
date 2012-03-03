package se.purplescout.purplemow.core.fsm.event;

public class Event {
	final public EventType type;
	final public EventPriority prio;

	public Event(EventType type, EventPriority prio) {
		this.type = type;
		this.prio = prio;
	}

	public Event(EventType type) {
		this.type = type;
		this.prio = EventPriority.DEFAULT;
	}
}
