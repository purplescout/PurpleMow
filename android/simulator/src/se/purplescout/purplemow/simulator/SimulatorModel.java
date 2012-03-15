package se.purplescout.purplemow.simulator;

import se.purplescout.purplemow.ComStream;
import java.io.IOException;

import android.util.Log;

public class SimulatorModel extends ComStream {

	private float x = 0.5f;
	private float y = 0.5f;
	private float direction;
	private int[] relays = new int[2];
	private int[] servos = new int[3];
	private byte[] sensorData;
	private long lastUpdate;
	
	SimulatorModel()
	{
		lastUpdate = System.currentTimeMillis();
		direction = (float) (2 * Math.random() * Math.PI);
	}

	@Override
	public void sendCommand(byte command, byte target, int value)
			throws IOException {
		Log.d(this.getClass().getName(), "sendCommand " + command + ", target = " + target + ", value = " + value);
		synchronized(this) {
			switch (command) {
			case RELAY_COMMAND:
				relays [target] = value;
				break;
			case SERVO_COMMAND:
				servos [target] = value;
				break;
			case 4: // read sensor
				generateSensorData(target, value);
				break;
			}
		}
	}

	@Override
	public void read(byte[] buffer) throws IOException {
		Log.d(this.getClass().getName(), "Trying to read sensor data");
		synchronized(this) {
			if (sensorData == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
			System.arraycopy(sensorData, 0, buffer, 0, sensorData.length);
			sensorData = null;
		}
		Log.d(this.getClass().getName(), "Sensor data read");
	}

	public ComStream getComStream() {
		return this;
	}

	public void update() {
		long now = System.currentTimeMillis();
		synchronized(this) {
			long timeDelta = now - lastUpdate;
			lastUpdate = now;
			final double speedFactor = 255*10000.0;
			final double turnFactor = 255*500.0;

			double speed = timeDelta * servos[SERVO1];
			if (relays[RELAY1] == 0 && relays[RELAY2] == 0) {
				x += speed * Math.cos(direction) / speedFactor;
				y += speed * Math.sin(direction) / speedFactor;
			} else if (relays[RELAY1] == 1 && relays[RELAY2] == 0) {
				direction += speed / turnFactor;
			} else if (relays[RELAY1] == 0 && relays[RELAY2] == 1) {
				direction -= speed / turnFactor;
			} else if (relays[RELAY1] == 1 && relays[RELAY2] == 1) {
				x -= speed * Math.cos(direction) / speedFactor;
				y -= speed * Math.sin(direction) / speedFactor;
			}
			if (speed != 0)
				Log.d(this.getClass().getName(), "Update: x = " + x + ", y = " + y + ", speed = " + speed + ", direction = " + direction);
		}
	}

	public synchronized float getMowerX() {
		return x;
	}

	public synchronized float getMowerY() {
		return y;
	}

	public synchronized float getDirection() {
		return direction;
	}

	private void generateSensorData(byte target, int value) {
		synchronized(this) {
			sensorData = new byte[4];
			sensorData[0] = 2;
			if (x < 0.1 || x > 0.9 || y < 0.1 || y > 0.9) {
				sensorData[3] = (byte) 99;
			} else {
				sensorData[3] = (byte) 255;
			}
			notify();
		}
	}
	
}
