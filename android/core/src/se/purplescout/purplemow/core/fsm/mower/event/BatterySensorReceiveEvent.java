package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class BatterySensorReceiveEvent extends CoreEvent<BatterySensorReceiveEventHandler> {

	public static final CoreEvent.Type<BatterySensorReceiveEventHandler> TYPE = new Type<BatterySensorReceiveEventHandler>();
	
	private final int value;
	
	public BatterySensorReceiveEvent(int value) {
		this.value = value;
	}

	@Override
	public void dispatch(BatterySensorReceiveEventHandler handler) {
		handler.onBatterySensorReceived(this);
	}

	@Override
	public CoreEvent.Type<BatterySensorReceiveEventHandler> getType() {
		return TYPE;
	}

	public int getValue() {
		return value;
	}
}
