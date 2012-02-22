package se.purplescout.purplemow;

import java.io.IOException;

public class MotorController {

	private static final int FULL_SPEED = 255;
	private static final int DELAY = 1000;
	private static final int STOP = 0;
	private ComStream comStream;
	private static MotorController mc = null;

	private MotorController() {

	}

	public static MotorController getInstance() {
		if (mc == null) {
			mc = new MotorController();
		}
		return mc;
	}

	public void moveForward(MotorState ms) throws IOException {
		move(ms, 1);
	}

	/**
	 * 
	 * @param ms
	 * @throws IOException
	 */
	public void stop(MotorState ms) throws IOException {
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO1, STOP);
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO2, STOP);
		sleep();
		getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
		getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);

		sleep();

		ms.setMotorLeft(0);
		ms.setMotorRight(0);
	}

	public void moveBackward(MotorState ms) throws IOException {
		move(ms, -1);
	}

	/**
	 * Om nån av motorerna går åt motsatt håll, stanna de först. Sedan byt riktning. Lägg på full speed ahead.
	 * 
	 * @param ms
	 * @param direction
	 * @throws IOException
	 */
	public void move(MotorState ms, int direction) throws IOException {
		if (ms.getMotorLeft() == -direction || ms.getMotorRight() == -direction) {
			stop(ms);
		}
		if (direction > 0) {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
		} else if (direction < 0) {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
		}
		sleep();
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO1, FULL_SPEED);
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO2, FULL_SPEED);

		ms.setMotorLeft(direction);
		ms.setMotorRight(direction);

		sleep();
	}

	public void turnRight(MotorState ms) throws IOException {
		turn(ms, true);
	}

	public void turnLeft(MotorState ms) throws IOException {
		turn(ms, false);
	}

	public void turn(MotorState ms, boolean right) throws IOException {
		if (ms.getMotorRight() != 0 || ms.getMotorLeft() != 0) {
			stop(ms);
		}

		if (right) {
			ms.setMotorLeft(1);
			getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO1, FULL_SPEED);
			sleep();
			stop(ms);
		} else {
			ms.setMotorRight(1);
			getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.SERVO2, FULL_SPEED);
			stop(ms);
		}
	}

	public void avoidObstacleLeftSide(MotorState ms) throws IOException {
		stop(ms);
		moveBackward(ms);
		sleep();
		sleep();
		turnRight(ms);
		stop(ms);
		moveForward(ms);
	}

	public void avoidObstacleRightSide(MotorState ms) throws IOException {
		stop(ms);
		moveBackward(ms);
		sleep();
		sleep();
		turnLeft(ms);
		stop(ms);
		moveForward(ms);
	}

	private void sleep() {
		try {
			Thread.sleep(DELAY);
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
