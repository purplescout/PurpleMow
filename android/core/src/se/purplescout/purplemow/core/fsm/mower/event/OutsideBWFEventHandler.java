package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface OutsideBWFEventHandler extends CoreEventHandler {

	void onOutSide(OutsideBWFEvent event);
}
