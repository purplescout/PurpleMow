package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface DecrementCutterSpeedEventHandler extends CoreEventHandler {

	void onDecrementCutterSpeed(DecrementCutterSpeedEvent event);
}
