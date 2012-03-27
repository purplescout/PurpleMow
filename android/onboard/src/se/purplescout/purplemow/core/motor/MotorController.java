package se.purplescout.purplemow.core.motor;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.fsm.MotorFSM.State;

public class MotorController {
	
	private static final int FULL_SPEED = 255;
	private static final int DELAY = 1000;
	private static final int STOP = 0;

	private ComStream comStream;
	
	public MotorController(ComStream comStream) {
		this.comStream = comStream;
	}

	public void moveForward(State state) throws IOException {
		move(state, 1);
	}

	/**
	 * 
	 * @param state
	 * @throws IOException
	 */
	public void stop() throws IOException {
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_RIGHT, STOP);
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_LEFT2, STOP);
		sleep();
	}

	public void moveBackward(State state) throws IOException {
		move(state, -1);
	}

	/**
	 * Om nån av motorerna går åt motsatt håll, stanna de först. Sedan byt
	 * riktning. Lägg på full speed ahead.
	 * 
	 * @param state
	 * @param direction
	 * @throws IOException
	 */
	public void move(State state, int direction) throws IOException {
		if (state != State.STILL) {
			stop();
		}
		if (direction > 0) {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
		} else if (direction < 0) {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
		}
		sleep();
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_RIGHT, FULL_SPEED);
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_LEFT2, FULL_SPEED);
	}

	public void turnRight(State state) throws IOException {
		turn(state, true);
	}

	public void turnLeft(State state) throws IOException {
		turn(state, false);
	}

	public void turn(State state, boolean right) throws IOException {
		if (state != State.STILL) {
			stop();
		}

		if (right) {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 1);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 0);
		} else {
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY1, 0);
			getComStream().sendCommand(ComStream.RELAY_COMMAND, ComStream.RELAY2, 1);
		}
		sleep();
		
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_RIGHT, FULL_SPEED);
		getComStream().sendCommand(ComStream.SERVO_COMMAND, ComStream.MOTOR_LEFT2, FULL_SPEED);
	}

	public void sleep() {
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
