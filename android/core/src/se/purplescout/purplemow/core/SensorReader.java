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

import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.controller.SensorLogger.SensorData;
import se.purplescout.purplemow.core.fsm.mower.event.BwfSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.RangeSensorReceiveEvent.Side;
import android.util.Log;

public class SensorReader extends Thread {

	private static final int SENSOR_BUFFER_SIZE = 10000;
	private static final int SLEEP_TIME = 10;
	private static final long SLEEP_TIME_LONG = 50;

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
	private CoreBus coreBus = CoreBus.getInstance();
	private Integer[] bwfVals = new Integer[] {1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023, 1023};

	public SensorReader(ComStream comStream) {
		this.comStream = comStream;
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				requestSensor(ComStream.ALL_SENSORS);
				readAllSensors();

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
				Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
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
				coreBus.fireEvent(new RangeSensorReceiveEvent(val, Side.LEFT));
			} else if (buffer[1] == ComStream.RANGE_SENSOR_RIGHT) {
				sensorData.get(ComStream.RANGE_SENSOR_RIGHT).add(new SensorData(new Date(), val));
				coreBus.fireEvent(new RangeSensorReceiveEvent(val, Side.RIGHT));
			} else if (buffer[1] == ComStream.BWF_SENSOR_LEFT) {
				bwfVals[1] = bwfVals[0];
				bwfVals[0] = val;
				sensorData.get(ComStream.BWF_SENSOR_LEFT).add(new SensorData(new Date(), getRunningAverage(bwfVals)));
				coreBus.fireEvent(new BwfSensorReceiveEvent(getRunningAverage(bwfVals)));
			}
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			handleIOException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readAllSensors() {
		byte[] buffer = new byte[12];
		try {
			comStream.read(buffer);
			byte hi = buffer[2];
			byte lo = buffer[3];
			int rangeRight = composeInt(hi, lo);

			hi = buffer[4];
			lo = buffer[5];
			int rangeLeft = composeInt(hi, lo);

			hi = buffer[6];
			lo = buffer[7];
			int bwf = composeInt(hi, lo);
			shiftArray(bwf);
			

			hi = buffer[8];
			lo = buffer[9];
			int voltage = composeInt(hi, lo);

			hi = buffer[10];
			lo = buffer[11];
			int current = composeInt(hi, lo);
			
			sensorData.get(ComStream.RANGE_SENSOR_LEFT).add(new SensorData(new Date(), rangeLeft));
			coreBus.fireEvent(new RangeSensorReceiveEvent(rangeLeft, Side.LEFT));

			sensorData.get(ComStream.RANGE_SENSOR_RIGHT).add(new SensorData(new Date(), rangeRight));
			coreBus.fireEvent(new RangeSensorReceiveEvent(rangeRight, Side.RIGHT));
			
			sensorData.get(ComStream.BWF_SENSOR_LEFT).add(new SensorData(new Date(), getRunningAverage(bwfVals)));
			sensorData.get(ComStream.BWF_SENSOR_RIGHT).add(new SensorData(new Date(), bwf));
			coreBus.fireEvent(new BwfSensorReceiveEvent(getRunningAverage(bwfVals)));

		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			handleIOException(e);	
		}		
	}
	/**
	 * Collect the latest 10 BWF values into an array
	 * @param bwf
	 */
	private void shiftArray(int bwf) {
		for (int i = bwfVals.length -1 ; i > 0; i --) {
			bwfVals[i] = bwfVals[i-1];
		}
		bwfVals[0] = bwf;
	}

	/**
	 * Calculate average based on the 10 latest bwf values
	 * @param bwfVals
	 * @return
	 */
	private int getRunningAverage(Integer[] bwfVals) {
		int sum = 0;
		for (Integer val : bwfVals) {
			sum += val;
		}
		
		return sum / bwfVals.length;
	}

	private void handleIOException(IOException e) {
		//TODO Notifiy system and log
		shutdown();
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
	public List<SensorData> getBwfSensorData() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.BWF_SENSOR_LEFT));
		Collections.reverse(values);
		return values;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getLeftRangeSensorData() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.RANGE_SENSOR_LEFT));
		Collections.reverse(values);
		return values;
	}

	@SuppressWarnings("unchecked")
	public List<SensorData> getRightRangeSensorData() {
		List<SensorData> values = new ArrayList<SensorData>(sensorData.get(ComStream.RANGE_SENSOR_RIGHT));
		Collections.reverse(values);
		return values;
	}
}
