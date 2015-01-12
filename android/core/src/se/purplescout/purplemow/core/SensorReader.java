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
import se.purplescout.purplemow.core.fsm.mower.event.ChargingDetectedEvent;
import se.purplescout.purplemow.core.fsm.mower.event.CutterFrequencyEvent;
import se.purplescout.purplemow.core.fsm.mower.event.NoBWFDataEvent;
import se.purplescout.purplemow.core.fsm.mower.event.OutsideBWFEvent;
import se.purplescout.purplemow.core.fsm.mower.event.PushButtonPressedEvent;
import android.util.Log;

public class SensorReader extends Thread {

	private static final int INSIDE_BWF = 215;
	private static final int OUTSIDE_BWF = 87;
	private static final int INSIDE_BWF_GOING_HOME = 213;
	private static final int OUTSIDE_BWF_GOING_HOME = 181;
	
	private static final int SENSOR_BUFFER_SIZE = 10000;
	private static final int SLEEP_TIME = 13;

	Map<Byte, Buffer> sensorData = new HashMap<Byte, Buffer>();
	{
		sensorData.put(ComStream.BWF_SENSOR_LEFT, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(SENSOR_BUFFER_SIZE)));
	}

	private ComStream comStream;

	private boolean isRunning = true;
	private int thrownExceptions = 0;
	private CoreBus coreBus = CoreBus.getInstance();
	private int batteryCounter = 0;
	private int frequencyCounter = 0;
	private long timer = System.currentTimeMillis();

	private int oldReading = 0;
	private static int TIMEOUT = 10000;

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
				// if more than 20 seconds then emergency stop
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

	
	private void readAllSensors() {
		byte[] buffer = new byte[16];
		try {
			comStream.read(buffer);
			
			//Cutter motor freq
			byte hi = buffer[6];
			byte lo = buffer[7];
			int frequency = composeInt(hi, lo);
			frequencyCounter++;
			//Hantera endast var 50 sample av klipparfrekvensen
			if (frequencyCounter  >= 50) {
//				Log.i(this.getClass().getSimpleName(), "Frekvens på klipparn: " + frequency);
				coreBus.fireEvent(new CutterFrequencyEvent(frequency));
				frequencyCounter = 0;
			}
			
			hi = buffer[8];
			lo = buffer[9];
			int voltage = composeInt(hi, lo);
			batteryCounter++;
			//Hantera endast var 100e sample av batterispänningen
			if (batteryCounter  >= 100) {
				//Log.i(this.getClass().getSimpleName(), "Batterispänning: " + voltage);
				coreBus.fireEvent(new BatterySensorReceiveEvent(voltage));
				batteryCounter = 0;
			}

			lo = buffer[11];
			if (lo == 0) { 
				coreBus.fireEvent(new ChargingDetectedEvent());
			}
			
			//Check bumper
			lo = buffer[12];
			if (lo == 0) { 
				coreBus.fireEvent(new BumperEvent());
			}
			
			//Check bwf sensor for outside/inside
			lo = buffer[14];
			
			Log.i(this.getClass().getSimpleName(), "BWF värde just nu: " + lo);
			int bwfVal =  lo;
			
			if (bwfVal < 0 ) {
				bwfVal = bwfVal + 256;
				Log.i(this.getClass().getSimpleName(), "Recalculated BWF " + bwfVal);
			}
			
			if(oldReading == bwfVal) {
				if (bwfVal == OUTSIDE_BWF || bwfVal == INSIDE_BWF ||
						bwfVal == OUTSIDE_BWF_GOING_HOME || bwfVal == INSIDE_BWF_GOING_HOME ) {
					coreBus.fireEvent(new OutsideBWFEvent(bwfVal));
				}
				handleTimer(bwfVal);
			}
			oldReading = bwfVal;
			checkTimer();
			
			//Check if push-button has been pressed
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

	private void handleTimer(int bwfVal) {
		if (bwfVal == INSIDE_BWF || bwfVal == INSIDE_BWF_GOING_HOME || bwfVal == OUTSIDE_BWF_GOING_HOME) {
			resetTimer();
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

}
