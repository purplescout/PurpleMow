package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface RangeSensorReceiveEventHandler extends CoreEventHandler {

	void onRangeSensorReceive(RangeSensorReceiveEvent event);
}
