package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class PushButtonPressedEvent extends CoreEvent<PushButtonPressedEventHandler> {

	public static final CoreEvent.Type<PushButtonPressedEventHandler> TYPE = new Type<PushButtonPressedEventHandler>();

	@Override
	public void dispatch(PushButtonPressedEventHandler handler) {
		handler.onButtonPressed(this);
	}

	@Override
	public CoreEvent.Type<PushButtonPressedEventHandler> getType() {
		return TYPE;
	}
}
