package se.purpleout.purplemow;

import java.io.IOException;

public abstract class ComStream {
	
	public static final byte SERVO1 = 1;
	public static final byte SERVO2 = 2;
	public static final byte SERVO3 = 0x12;
	public static final byte RELAY1 = 0;
	public static final byte RELAY2 = 1;
	
	public static final byte SERVO_COMMAND = 2;
	public static final byte RELAY_COMMAND = 3;

	public abstract void sendCommand(byte command, byte target, int value) throws IOException;

	public abstract void read(byte[] buffer) throws IOException;
	
	public void readSensor()  throws IOException {
		sendCommand((byte)4, (byte)0, (byte)1);
	}
}
