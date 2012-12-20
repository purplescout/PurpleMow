package se.purplescout.purplemow.core.fsm.common.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface NewConstantsEventHandler extends CoreEventHandler {

	void onNewConstants(NewConstantsEvent event);
}
