package se.purplescout.purplemow.core;

public class Constants {
	public static final int FULL_SPEED = 255;
	public static final int STOP = 0;

	public static final int TOO_DARN_CLOSE = 380;

	public static final byte SENSOR_BWF = 0;

	// public static final byte SENSOR_RANGE = 0;

	public enum Event {
		START, STOP, BWF_SENSOR, DIST_SENSOR, TIMER
	}

	public enum Direction {
		FORWARD, BACKWARD, LEFT, RIGHT
	}

}
