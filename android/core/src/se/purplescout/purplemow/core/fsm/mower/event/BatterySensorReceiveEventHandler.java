package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface BatterySensorReceiveEventHandler extends CoreEventHandler {

	void onBatterySensorReceived(BatterySensorReceiveEvent event);
}
