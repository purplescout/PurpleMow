package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface PushButtonPressedEventHandler extends CoreEventHandler {

	void onButtonPressed(PushButtonPressedEvent event);
}
