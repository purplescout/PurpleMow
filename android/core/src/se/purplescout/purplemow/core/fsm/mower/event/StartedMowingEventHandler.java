package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface StartedMowingEventHandler extends CoreEventHandler {

	void onStartedMowing(StartedMowingEvent event);
}
