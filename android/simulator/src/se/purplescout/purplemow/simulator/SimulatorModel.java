package se.purplescout.purplemow.simulator;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.Constants;
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
			case DIRECTION_COMMAND:
				relays [target] = value;
				break;
			case MOTOR_COMMAND:
				servos [target] = value;
				break;
			case SENSOR_COMMAND: // read sensor
				generateSensorData(target);
				break;
			}
		}
	}
	
	@Override
	public void sendCommand(byte command, byte target) throws IOException {
		sendCommand(command, target, -1);	
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

			double speed = timeDelta * servos[MOTOR_RIGHT];
			if (relays[MOTOR_RIGHT] == 0 && relays[MOTOR_LEFT] == 0) {
				x += speed * Math.cos(direction) / speedFactor;
				y += speed * Math.sin(direction) / speedFactor;
			} else if (relays[MOTOR_RIGHT] == 1 && relays[MOTOR_LEFT] == 0) {
				direction += speed / turnFactor;
			} else if (relays[MOTOR_RIGHT] == 0 && relays[MOTOR_LEFT] == 1) {
				direction -= speed / turnFactor;
			} else if (relays[MOTOR_RIGHT] == 1 && relays[MOTOR_LEFT] == 1) {
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

	private void generateSensorData(byte target) {
		synchronized(this) {
			sensorData = new byte[4];
			//sensorData[0] = ?;
			sensorData[1] = target;
			int value = 0;
			switch (target) {
			case BWF_SENSOR_LEFT:
			case BWF_SENSOR_RIGHT:
				if (x < 0.1 || x > 0.9 || y < 0.1 || y > 0.9) {
					value = Constants.BWF_LIMIT - 10;
				} else {
					value = Constants.BWF_LIMIT + 10;
				}
				break;
			}
			sensorData[2] = (byte) (value >> 8);
			sensorData[3] = (byte) (value & 0xFF);
			notify();
		}
	}	
}
