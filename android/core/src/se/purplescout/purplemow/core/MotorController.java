package se.purplescout.purplemow.core;

import static se.purplescout.purplemow.core.common.Constants.FULL_SPEED;

import java.io.IOException;

import se.purplescout.purplemow.core.common.Constants;

public class MotorController {
	public enum Direction {
		FORWARD, BACKWARD, LEFT, RIGHT
	}

	private boolean moving;

	private ComStream comStream;

	public MotorController(ComStream comStream) {
		this.comStream = comStream;
	}

	public void move(int speed) throws IOException {
		if (speed > Constants.FULL_SPEED) {
			speed = Constants.FULL_SPEED;
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
		// sleep(300);
	}

	public void runCutter(int speed) throws IOException {
		comStream.sendCommand(ComStream.MOTOR_COMMAND, ComStream.CUTTER_MOTOR, speed);
	}

	void testPattern() {
		try {
			move(FULL_SPEED);
			sleep(3000);

			move(0);
			sleep(100);

			setDirection(Direction.BACKWARD);
			sleep(100);
			move(FULL_SPEED);
			sleep(1500);

			move(0);
			sleep(100);

			setDirection(Direction.RIGHT);
			sleep(100);
			move(FULL_SPEED);
			sleep(800);

			move(0);
			sleep(100);

			setDirection(Direction.FORWARD);
			sleep(100);
			move(FULL_SPEED);
			sleep(2000);

			move(0);
			sleep(100);

			setDirection(Direction.LEFT);
			sleep(100);
			move(FULL_SPEED);
			sleep(800);

			move(0);
			sleep(100);

			setDirection(Direction.FORWARD);
			sleep(100);
			move(FULL_SPEED);
			sleep(2000);

			move(0);
			sleep(100);

		} catch (IOException e) {
		}
	}

	// Only used for testPattern
	void sleep(int delayMillis) {
		try {
			Thread.sleep(delayMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ComStream getComStream() {
		return comStream;
	}

	public void setComStream(ComStream comStream) {
		this.comStream = comStream;
	}
}
