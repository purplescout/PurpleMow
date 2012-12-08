package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class RangeSensorReceiveEvent extends CoreEvent<RangeSensorReceiveEventHandler> {

	public static final CoreEvent.Type<RangeSensorReceiveEventHandler> TYPE = new Type<RangeSensorReceiveEventHandler>();

	public enum Side {
		LEFT, RIGHT
	}

	private final int value;
	private final Side side;

	public RangeSensorReceiveEvent(int value, Side side) {
		this.value = value;
		this.side = side;
	}

	@Override
	public void dispatch(RangeSensorReceiveEventHandler handler) {
		handler.onRangeSensorReceive(this);
	}

	@Override
	public CoreEvent.Type<RangeSensorReceiveEventHandler> getType() {
		return TYPE;
	}

	public int getValue() {
		return value;
	}

	public Side getSide() {
		return side;
	}
}
