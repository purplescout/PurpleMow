package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class NoBWFDataEvent extends CoreEvent<NoBWFDataEventHandler> {
	

	public static final CoreEvent.Type<NoBWFDataEventHandler> TYPE = new Type<NoBWFDataEventHandler>();
	private int bwfVal = 0;

	@Override
	public void dispatch(NoBWFDataEventHandler handler) {
		handler.onNoData(this);
	}

	@Override
	public CoreEvent.Type<NoBWFDataEventHandler> getType() {
		return TYPE;
	}

}
