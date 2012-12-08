package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class MowerChangeStateEvent extends CoreEvent<MowerChangeStateEventHandler> {

	public static final CoreEvent.Type<MowerChangeStateEventHandler> TYPE = new Type<MowerChangeStateEventHandler>();

	private final String oldState;
	private final String newState;

	public MowerChangeStateEvent(String oldState, String newState) {
		this.oldState = oldState;
		this.newState = newState;
	}

	@Override
	public void dispatch(MowerChangeStateEventHandler handler) {
		handler.onMowerChangeState(this);
	}

	@Override
	public CoreEvent.Type<MowerChangeStateEventHandler> getType() {
		return TYPE;
	}

	public String getNewState() {
		return newState;
	}

	public String getOldState() {
		return oldState;
	}
}
