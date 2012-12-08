package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class MowEvent extends CoreEvent<MowEventHandler> {

	public static final CoreEvent.Type<MowEventHandler> TYPE = new Type<MowEventHandler>();

	private final int velocity;

	public MowEvent(int velocity) {
		this.velocity = velocity;
	}

	@Override
	public void dispatch(MowEventHandler handler) {
		handler.onMow(this);
	}

	@Override
	public CoreEvent.Type<MowEventHandler> getType() {
		return TYPE;
	}

	public int getVelocity() {
		return velocity;
	}
}
