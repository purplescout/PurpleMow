package se.purplescout.purplemow.core;

import java.io.IOException;

import se.purplescout.purplemow.core.common.Constants;

public class MotorController {
	public enum Direction {
		FORWARD, BACKWARD, LEFT, RIGHT
	}

	private boolean moving;

	private final ComStream comStream;
	private Constants constants;

	public MotorController(ComStream comStream, Constants constants) {
		this.comStream = comStream;
		this.constants = constants;
	}

	public void updateConstants(Constants constants) {
		this.constants = constants;
	}

	public void move(int speed) throws IOException {
		if (speed > constants.getFullSpeed()) {
			speed = constants.getFullSpeed();
		}
		if (speed < 0) {
			speed = 0;
		}
		comStream.sendCommand(ComStream.MOTOR_COMMAND, ComStream.MOTOR_LEFT, speed);
		comStream.sendCommand(ComStream.MOTOR_COMMAND, ComStream.MOTOR_RIGHT, speed);

		moving = (speed > 0);
	}

	public void setDirection(Direction direction) throws IOException {
		assert (!moving);

		switch (direction) {
		case FORWARD:
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_RIGHT, 0);
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_LEFT, 0);
			break;
		case BACKWARD:
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_RIGHT, 1);
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_LEFT, 1);
			break;
		case LEFT:
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_RIGHT, 0);
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_LEFT, 1);
			break;
		case RIGHT:
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_RIGHT, 1);
			comStream.sendCommand(ComStream.DIRECTION_COMMAND, ComStream.MOTOR_LEFT, 0);
			break;
		}
	}

	public void runCutter(int speed) throws IOException {
		comStream.sendCommand(ComStream.MOTOR_COMMAND, ComStream.CUTTER_MOTOR, speed);
	}
}
