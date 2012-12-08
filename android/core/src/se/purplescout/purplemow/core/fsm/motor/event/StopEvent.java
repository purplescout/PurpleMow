package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class StopEvent extends CoreEvent<StopEventHandler> {

	public static final CoreEvent.Type<StopEventHandler> TYPE = new Type<StopEventHandler>();

	@Override
	public void dispatch(StopEventHandler handler) {
		handler.onStop(this);
	}

	@Override
	public CoreEvent.Type<StopEventHandler> getType() {
		return TYPE;
	}
}
