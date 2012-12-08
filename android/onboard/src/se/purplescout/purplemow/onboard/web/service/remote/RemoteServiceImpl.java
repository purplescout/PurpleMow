package se.purplescout.purplemow.onboard.web.service.remote;

import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.fsm.motor.event.StopEvent;
import se.purplescout.purplemow.core.fsm.mower.event.DecrementCutterSpeedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementCutterSpeedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.IncrementMovementSpeedEvent;

public class RemoteServiceImpl implements RemoteService {

	private CoreBus coreBus = CoreBus.getInstance();

	@Override
	public void incrementMovmentSpeed(Direction direction) {
		switch (direction) {
		case FORWARD:
			coreBus.fireEvent(new IncrementMovementSpeedEvent(se.purplescout.purplemow.core.MotorController.Direction.FORWARD));
			break;
		case REVERSE:
			coreBus.fireEvent(new IncrementMovementSpeedEvent(se.purplescout.purplemow.core.MotorController.Direction.BACKWARD));
			break;
		case LEFT:
			coreBus.fireEvent(new IncrementMovementSpeedEvent(se.purplescout.purplemow.core.MotorController.Direction.LEFT));
			break;
		case RIGHT:
			coreBus.fireEvent(new IncrementMovementSpeedEvent(se.purplescout.purplemow.core.MotorController.Direction.RIGHT));
			break;
		}
	}

	@Override
	public void stop() {
		coreBus.fireEvent(new StopEvent());
	}

	@Override
	public void incrementCutterSpeed() {
		coreBus.fireEvent(new IncrementCutterSpeedEvent());
	}

	@Override
	public void decrementCutterSpeed() {
		coreBus.fireEvent(new DecrementCutterSpeedEvent());
	}
}
