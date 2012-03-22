package se.purplescout.purplemow.core;

import static se.purplescout.purplemow.core.Constants.FULL_SPEED;

import java.io.IOException;

import se.purplescout.purplemow.core.Constants.Direction;

public class MotorController {

	private boolean moving;

	private ComStream comStream;

	public MotorController(ComStream comStream) {
		this.comStream = comStream;
	}

	void move(int speed) throws IOException {
		comStream.sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_RIGHT, speed);
		comStream.sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_LEFT2, speed);

		moving = (speed > 0);
	}

	void setDirection(Direction direction) throws IOException {
		assert (!moving);

		switch (direction) {
		case FORWARD:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
			break;
		case BACKWARD:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
			break;
		case LEFT:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
			break;
		case RIGHT:
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			comStream.sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
			break;
		}
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
