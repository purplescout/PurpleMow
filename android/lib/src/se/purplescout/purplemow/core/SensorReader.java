package se.purplescout.purplemow.core;

import java.io.IOException;

import se.purplescout.purplemow.core.fsm.AbstractFSM;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import android.util.Log;

public class SensorReader extends Thread {

	private static final int SLEEP_TIME = 100;

	private ComStream comStream;

	private Integer latestDistanceValue = 0;

	private byte hiB;
	private byte loB;
	private Integer latestBWFValue = 0;
	private boolean isRunning = true;
	private int thrownExceptions = 0;

	AbstractFSM<MainFSMEvent> mainFSM;

	public SensorReader(ComStream comStream) {
		this.comStream = comStream;
	}

	public void setMainFSM(AbstractFSM<MainFSMEvent> fsm) {
		mainFSM = fsm;
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				requestSensor(ComStream.RANGE_SENSOR_LEFT);
				readSensor();

				Thread.sleep(SLEEP_TIME);

				requestSensor(ComStream.RANGE_SENSOR_RIGHT);
				readSensor();

				Thread.sleep(SLEEP_TIME);

				requestSensor(ComStream.BWF_SENSOR_RIGHT);
				readSensor();

				Thread.sleep(SLEEP_TIME);

				requestSensor(ComStream.BWF_SENSOR_LEFT);
				readSensor();

				Thread.sleep(SLEEP_TIME);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void requestSensor(byte sensor) {
		try {
			comStream.sendCommand(ComStream.SENSOR_COMMAND, sensor);
		} catch (IOException e) {
			// Prevents flooding the log
			if (thrownExceptions < 2) {
				Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
				thrownExceptions++;
			}
		}
	}

	private void readSensor() {
		try {
			byte[] buffer = new byte[4];
			comStream.read(buffer);
			byte hi = buffer[2];
			byte lo = buffer[3];
			this.hiB = hi;
			this.loB = lo;
			int val = composeInt(hi, lo);
			latestDistanceValue = val;
			if (buffer[1] == ComStream.RANGE_SENSOR_LEFT) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.RANGE_LEFT, val));
			} else if (buffer[1] == ComStream.RANGE_SENSOR_RIGHT) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.RANGE_RIGHT, val));
			} else if (buffer[1] == ComStream.BWF_SENSOR_RIGHT) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.BWF_RIGHT, val));
			} else if (buffer[1] == ComStream.BWF_SENSOR_LEFT) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.BWF_LEFT, val));
			}
		} catch (IOException e) {
			// Prevents flooding the log
			if (thrownExceptions < 2) {
				Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
				thrownExceptions++;
			}
		}
	}

	public int getLatestDistanceValue() {
		return latestDistanceValue;
	}

	public int getLatestBWFValue() {
		return latestBWFValue;
	}

	private int composeInt(byte hi, byte lo) {
		int val = hi & 0xff;
		val *= 256;
		val += lo & 0xff;
		return val;
	}

	public byte getHiB() {
		return hiB;
	}

	public byte getLoB() {
		return loB;
	}

	public void shutdown() {
		isRunning = false;
	}
}
