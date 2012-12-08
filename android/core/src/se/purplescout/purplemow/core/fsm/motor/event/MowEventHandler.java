package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface MowEventHandler extends CoreEventHandler {

	void onMow(MowEvent event);
}
