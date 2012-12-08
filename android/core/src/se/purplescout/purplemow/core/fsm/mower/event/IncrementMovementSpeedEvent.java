package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class IncrementMovementSpeedEvent extends CoreEvent<IncrementMovementSpeedEventHandler> {

	public static final CoreEvent.Type<IncrementMovementSpeedEventHandler> TYPE = new Type<IncrementMovementSpeedEventHandler>();

	private final Direction direction;
	
	public IncrementMovementSpeedEvent(Direction direction) {
		this.direction = direction;
	}

	@Override
	public void dispatch(IncrementMovementSpeedEventHandler handler) {
		handler.onIncrementMovementSpeed(this);
	}

	@Override
	public CoreEvent.Type<IncrementMovementSpeedEventHandler> getType() {
		return TYPE;
	}

	public Direction getDirection() {
		return direction;
	}
}
