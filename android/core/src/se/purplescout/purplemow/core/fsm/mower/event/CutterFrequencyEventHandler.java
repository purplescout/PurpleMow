package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface CutterFrequencyEventHandler extends CoreEventHandler {
	
	void onCutterFreqeuncyEvent(CutterFrequencyEvent event);

}
