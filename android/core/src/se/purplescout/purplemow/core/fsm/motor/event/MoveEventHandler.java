package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface MoveEventHandler extends CoreEventHandler {

	void onMove(MoveEvent event);
}
