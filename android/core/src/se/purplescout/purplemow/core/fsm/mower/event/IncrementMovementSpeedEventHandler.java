package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface IncrementMovementSpeedEventHandler extends CoreEventHandler {

	void onIncrementMovementSpeed(IncrementMovementSpeedEvent event);
}
