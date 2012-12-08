package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class BwfSensorReceiveEvent extends CoreEvent<BwfSensorReceiveEventHandler> {

	public static final CoreEvent.Type<BwfSensorReceiveEventHandler> TYPE = new Type<BwfSensorReceiveEventHandler>();

	private final int value;

	public BwfSensorReceiveEvent(int value) {
		this.value = value;
	}

	@Override
	public void dispatch(BwfSensorReceiveEventHandler handler) {
		handler.onMowerChangeState(this);
	}

	@Override
	public CoreEvent.Type<BwfSensorReceiveEventHandler> getType() {
		return TYPE;
	}

	public int getValue() {
		return value;
	}
}
