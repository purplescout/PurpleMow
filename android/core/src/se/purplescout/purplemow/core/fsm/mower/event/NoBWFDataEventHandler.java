package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface NoBWFDataEventHandler extends CoreEventHandler {

	void onNoData(NoBWFDataEvent event);
}
