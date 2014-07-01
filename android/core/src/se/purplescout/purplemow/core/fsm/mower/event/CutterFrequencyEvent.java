package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;


public class CutterFrequencyEvent extends CoreEvent<CutterFrequencyEventHandler> {

	public static final CoreEvent.Type<CutterFrequencyEventHandler> TYPE = new Type<CutterFrequencyEventHandler>();

	private final int value;

	public CutterFrequencyEvent(int value) {
		this.value = value;
	}

	@Override
	public void dispatch(CutterFrequencyEventHandler handler) {
		handler.onCutterFreqeuncyEvent(this);
	}

	@Override
	public CoreEvent.Type<CutterFrequencyEventHandler> getType() {
		return TYPE;
	}

	public int getValue() {
		return value;
	}
	


}
