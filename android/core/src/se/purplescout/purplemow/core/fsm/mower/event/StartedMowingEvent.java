package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class StartedMowingEvent extends CoreEvent<StartedMowingEventHandler> {

	public static final CoreEvent.Type<StartedMowingEventHandler> TYPE = new Type<StartedMowingEventHandler>();

	@Override
	public void dispatch(StartedMowingEventHandler handler) {
		handler.onStartedMowing(this);
	}

	@Override
	public CoreEvent.Type<StartedMowingEventHandler> getType() {
		return TYPE;
	}
}
