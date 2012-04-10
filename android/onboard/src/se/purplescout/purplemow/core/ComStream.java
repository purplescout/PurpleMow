package se.purplescout.purplemow.core;

import java.io.IOException;

public abstract class ComStream {

	public static final byte SERVO_COMMAND = 0x2;
	public static final byte SERVO1 = 0;
	public static final byte SERVO2 = 1;
	public static final byte SERVO3 = 2;

	public static final byte RELAY_COMMAND = 0x3;
	public static final byte RELAY1 = 0;
	public static final byte RELAY2 = 1;

	public static final byte SENSOR_COMMAND = 0x4;
	public static final byte RANGE_SENSOR = 0x0;
	public static final byte MOIST_SENSOR = 0x1;
	public static final byte VOLTAGE_SENSOR = 0x2;
	public static final byte BWF_SENSOR_RIGHT = 0x3;
	public static final byte BWF_SENSOR_LEFT = 0x4;

	public abstract void sendCommand(byte command, byte target, int value) throws IOException;

	public abstract void sendCommand(byte command, byte target) throws IOException;

	public abstract void read(byte[] buffer) throws IOException;
}
