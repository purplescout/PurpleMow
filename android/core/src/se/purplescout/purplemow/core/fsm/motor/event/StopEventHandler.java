package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface StopEventHandler extends CoreEventHandler {

	void onStop(StopEvent event);
}
