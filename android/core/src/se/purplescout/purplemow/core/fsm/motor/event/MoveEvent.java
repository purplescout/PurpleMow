package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class MoveEvent extends CoreEvent<MoveEventHandler> {

	public static final CoreEvent.Type<MoveEventHandler> TYPE = new Type<MoveEventHandler>();

	private final int velocity;
	private final Direction direction;

	public MoveEvent(int velocity, Direction direction) {
		this.velocity = velocity;
		this.direction = direction;
	}

	@Override
	public void dispatch(MoveEventHandler handler) {
		handler.onMove(this);
	}

	@Override
	public CoreEvent.Type<MoveEventHandler> getType() {
		return TYPE;
	}

	public int getVelocity() {
		return velocity;
	}

	public Direction getDirection() {
		return direction;
	}
}
