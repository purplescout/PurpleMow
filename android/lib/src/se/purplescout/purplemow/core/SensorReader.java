package se.purplescout.purplemow.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import se.purplescout.purplemow.core.fsm.AbstractFSM;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import android.util.Log;

public class SensorReader extends Thread {

	private static final int SENSOR_BUFFER_SIZE = 10000;
	private static final int SLEEP_TIME = 20;
	private static final long SLEEP_TIME_LONG = 100;

	public class SensorData {

		private Date date;
		private int value;

		public SensorData(Date date, int value) {
			this.date = date;
			this.value = value;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	Map<Byte, Buffer> sensorData = new HashMap<Byte, Buffer>();
	{
		sensorData.put(ComStream.BWF_SENSOR_LEFT, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(SENSOR_BUFFER_SIZE)));
		sensorData.put(ComStream.BWF_SENSOR_RIGHT, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(SENSOR_BUFFER_SIZE)));
		sensorData.put(ComStream.RANGE_SENSOR_LEFT, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(SENSOR_BUFFER_SIZE)));
		sensorData.put(ComStream.RANGE_SENSOR_RIGHT, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(SENSOR_BUFFER_SIZE)));
	}

	private ComStream comStream;

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

				Thread.sleep(SLEEP_TIME_LONG);
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

	@SuppressWarnings("unchecked")
	private void readSensor() {
		try {
			byte[] buffer = new byte[4];
			comStream.read(buffer);
			byte hi = buffer[2];
			byte lo = buffer[3];
			int val = composeInt(hi, lo);
			if (buffer[1] == ComStream.RANGE_SENSOR_LEFT) {
				sensorData.get(ComStream.RANGE_SENSOR_LEFT).add(new SensorData(new Date(), val));
				mainFSM.queueEvent(new MainFSMEvent(EventType.RANGE_LEFT, val));
			} else if (buffer[1] == ComStream.RANGE_SENSOR_RIGHT) {
				sensorData.get(ComStream.RANGE_SENSOR_RIGHT).add(new SensorData(new Date(), val));
				mainFSM.queueEvent(new MainFSMEvent(EventType.RANGE_RIGHT, val));
			} else if (buffer[1] == ComStream.BWF_SENSOR_RIGHT) {
				sensorData.get(ComStream.BWF_SENSOR_RIGHT).add(new SensorData(new Date(), val));
				mainFSM.queueEvent(new MainFSMEvent(EventType.BWF_RIGHT, val));
			} else if (buffer[1] == ComStream.BWF_SENSOR_LEFT) {
				sensorData.get(ComStream.BWF_SENSOR_LEFT).add(new SensorData(new Date(), val));
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

	private int composeInt(byte hi, byte lo) {
		int val = hi & 0xff;
		val *= 256;
		val += lo & 0xff;
		return val;
	}

	public void shutdown() {
		isRunning = false;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getBwfLeft() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.BWF_SENSOR_LEFT));
		Collections.reverse(values);
		return values;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getBwfRight() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.BWF_SENSOR_RIGHT));
		Collections.reverse(values);
		return values;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getRangeLeft() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.RANGE_SENSOR_LEFT));
		Collections.reverse(values);
		return values;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getRangeRight() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.RANGE_SENSOR_RIGHT));
		Collections.reverse(values);
		return values;
	}
}
