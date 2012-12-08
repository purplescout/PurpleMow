package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class IncrementCutterSpeedEvent extends CoreEvent<IncrementCutterSpeedEventHandler> {

	public static final CoreEvent.Type<IncrementCutterSpeedEventHandler> TYPE = new Type<IncrementCutterSpeedEventHandler>();

	@Override
	public void dispatch(IncrementCutterSpeedEventHandler handler) {
		handler.onIncrementCutterSpeed(this);
	}

	@Override
	public CoreEvent.Type<IncrementCutterSpeedEventHandler> getType() {
		return TYPE;
	}
}
