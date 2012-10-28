package se.purplescout.purplemow.core.fsm.event;


public abstract class FSMEvent<ET, E> implements Comparable<E> {

	protected final ET eventType;
	protected final int value;
	protected long timeStamp;

	public FSMEvent(ET eventType) {
		this(eventType, -1);
	}

	public FSMEvent(ET eventType, int value) {
		this.eventType = eventType;
		this.value = value;
	}

	public ET getEventType() {
		return eventType;
	}

	public int getValue() {
		return value;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
}
