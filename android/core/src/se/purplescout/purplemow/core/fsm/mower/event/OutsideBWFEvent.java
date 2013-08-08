package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class OutsideBWFEvent extends CoreEvent<OutsideBWFEventHandler> {
	

	public static final CoreEvent.Type<OutsideBWFEventHandler> TYPE = new Type<OutsideBWFEventHandler>();
	private int bwfVal = 0;

	public OutsideBWFEvent(int bwfVal) {
		this.setBwfVal(bwfVal);
	}


	@Override
	public void dispatch(OutsideBWFEventHandler handler) {
		handler.onOutSide(this);
	}

	@Override
	public CoreEvent.Type<OutsideBWFEventHandler> getType() {
		return TYPE;
	}


	public int getBwfVal() {
		return bwfVal;
	}


	public void setBwfVal(int bwfVal) {
		this.bwfVal = bwfVal;
	}

}
