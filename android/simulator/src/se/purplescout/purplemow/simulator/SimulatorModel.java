package se.purplescout.purplemow.simulator;

import se.purplescout.purplemow.ComStream;
import java.io.IOException;

import android.util.Log;

public class SimulatorModel extends ComStream {

	private float x = 0.5f;
	private float y = 0.5f;
	private float direction = 1;
	private int[] relays = new int[2];
	private int[] servos = new int[3];
	private byte[] sensorData;
	private long lastUpdate;

	@Override
	public void sendCommand(byte command, byte target, int value)
			throws IOException {
		Log.d(this.getClass().getName(), "sendCommand " + command);
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

			float speed = timeDelta * servos[SERVO1] / (255*10000.0f);
			float speedX = 0, speedY = 0;
			if (relays[RELAY1] == 0 && relays[RELAY2] == 0) {
				speedX = (float) (speed * Math.cos(direction));
				speedY = (float) (speed * Math.sin(direction));
			} else if (relays[RELAY1] == 1 && relays[RELAY2] == 0) {
				direction += 0.01;
			} else if (relays[RELAY1] == 0 && relays[RELAY2] == 1) {
				direction -= 0.01;
			} else if (relays[RELAY1] == 1 && relays[RELAY2] == 1) {
				speedX = (float) (-speed * Math.cos(direction));
				speedY = (float) (-speed * Math.sin(direction));
			}
			x += speedX;
			y += speedY;
		}
	}

	public float getMowerX() {
		return x;
	}

	public float getMowerY() {
		return y;
	}

	private void generateSensorData(byte target, int value) {
		synchronized(this) {
			sensorData = new byte[4];				
			if (x < 0.1 || x > 0.9 || y < 0.1 || y > 0.9) {
				sensorData[3] = (byte) 99;
			} else {
				sensorData[3] = (byte) 255;
			}
			notify();
		}
	}
	
}
