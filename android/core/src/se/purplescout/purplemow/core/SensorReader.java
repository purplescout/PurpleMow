package se.purplescout.purplemow.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.controller.SensorLogger.SensorData;
import se.purplescout.purplemow.core.fsm.mower.event.BatterySensorReceiveEvent;
import se.purplescout.purplemow.core.fsm.mower.event.BumperEvent;
import se.purplescout.purplemow.core.fsm.mower.event.NoBWFDataEvent;
import se.purplescout.purplemow.core.fsm.mower.event.OutsideBWFEvent;
import se.purplescout.purplemow.core.fsm.mower.event.PushButtonPressedEvent;
import android.util.Log;

public class SensorReader extends Thread {

	private static final int SENSOR_BUFFER_SIZE = 10000;
	private static final int SLEEP_TIME = 13;
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
	private int batteryCounter = 0;
	private long timer = System.currentTimeMillis();
	private static int TIMEOUT = 20000;

	public SensorReader(ComStream comStream) {
		this.comStream = comStream;
	}

	@Override
	public void run() {
		try {
			while (isRunning) {
				requestSensor(ComStream.ALL_SENSORS);
				readAllSensors();
				//Check duration of last time there was an InsideBWF value received
				// if more than 7 seconds then emergency stop
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
	private void readAllSensors() {
		byte[] buffer = new byte[16];
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
			batteryCounter++;
			//Hantera endast var 50e sample av batterispänningen
			if (batteryCounter  >= 100) {
				//Log.i(this.getClass().getSimpleName(), "Batterispänning: " + voltage);
				coreBus.fireEvent(new BatterySensorReceiveEvent(voltage));
				batteryCounter = 0;
			}

			hi = buffer[10];
			lo = buffer[11];
			int current = composeInt(hi, lo);
			
			//Check bumper
			lo = buffer[12];
			if (lo == 0) { 
				coreBus.fireEvent(new BumperEvent());
			}
			
			//Check bwf sensor for outside/inside
			lo = buffer[14];
			int bwfVal =  lo;
			
			if (bwfVal < 0 ) {
				//Log.i(this.getClass().getSimpleName(), "Recalculating BWF");
				bwfVal = bwfVal + 256;
			}
			//171 utanför, 107 innanför
			if (bwfVal == 171 || bwfVal == 107 ) {
				//Log.i(this.getClass().getSimpleName(), "Close to BWF: " + bwfVal);
				coreBus.fireEvent(new OutsideBWFEvent(bwfVal));
			}

			if (bwfVal == 107) {
				resetTimer();
			} else {
				checkTimer();
			}

			//Check if inside value has been missing for long...
//			lo = buffer[15];
//			if (lo == 1) {
//				coreBus.fireEvent(new NoBWFDataEvent());
//			}
			
			//Check if push-button has been set
			lo = buffer[13];
			if (lo == 0) {
				resetTimer();
				coreBus.fireEvent(new PushButtonPressedEvent());
			}			


		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			handleIOException(e);	
		}		
	}
	private void checkTimer() {
		if(timer + TIMEOUT < System.currentTimeMillis()) {
			coreBus.fireEvent(new NoBWFDataEvent());
		}
	}

	private void resetTimer() {
		timer = System.currentTimeMillis();
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
