package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface IncrementCutterSpeedEventHandler extends CoreEventHandler {

	void onIncrementCutterSpeed(IncrementCutterSpeedEvent event);
}
