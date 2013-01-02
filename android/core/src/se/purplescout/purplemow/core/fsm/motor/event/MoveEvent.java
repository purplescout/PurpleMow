package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.MotorController.Direction;
import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class MoveEvent extends CoreEvent<MoveEventHandler> {

	public static final CoreEvent.Type<MoveEventHandler> TYPE = new Type<MoveEventHandler>();
	
	private final int speedRight;
	private final int speedLeft;
	private final Direction direction;

	public MoveEvent(int speed, Direction direction) {
		this.speedRight = speed;
		this.speedLeft = speed;
		this.direction = direction;
	}
	public MoveEvent(int speedRight, int speedLeft, Direction direction) {
		this.speedRight = speedRight;
		this.speedLeft = speedLeft;
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

	public Direction getDirection() {
		return direction;
	}
	public int getSpeedRight() {
		return speedRight;
	}
	public int getSpeedLeft() {
		return speedLeft;
	}
}
