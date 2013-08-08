package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class BumperEvent extends CoreEvent<BumperEventHandler> {

	public static final CoreEvent.Type<BumperEventHandler> TYPE = new Type<BumperEventHandler>();

	@Override
	public void dispatch(BumperEventHandler handler) {
		handler.onBumperPressed(this);
	}

	@Override
	public CoreEvent.Type<BumperEventHandler> getType() {
		return TYPE;
	}
}
