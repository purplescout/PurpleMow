package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface BwfSensorReceiveEventHandler extends CoreEventHandler {

	void onMowerChangeState(BwfSensorReceiveEvent event);
}
