package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface BumperEventHandler extends CoreEventHandler {

	void onBumperPressed(BumperEvent event);
}
