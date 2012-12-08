package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class DecrementCutterSpeedEvent extends CoreEvent<DecrementCutterSpeedEventHandler> {

	public static final CoreEvent.Type<DecrementCutterSpeedEventHandler> TYPE = new Type<DecrementCutterSpeedEventHandler>();

	@Override
	public void dispatch(DecrementCutterSpeedEventHandler handler) {
		handler.onDecrementCutterSpeed(this);
	}

	@Override
	public CoreEvent.Type<DecrementCutterSpeedEventHandler> getType() {
		return TYPE;
	}
}
