package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface MowerChangeStateEventHandler extends CoreEventHandler {

	void onMowerChangeState(MowerChangeStateEvent event);
}
