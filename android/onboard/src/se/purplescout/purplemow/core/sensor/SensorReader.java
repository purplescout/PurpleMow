package se.purplescout.purplemow.core.sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import se.purplescout.purplemow.core.ComStream;
import android.util.Log;

public class SensorReader extends TimerTask {

	private ComStream comStream;
	private List<Runnable> sensors = new ArrayList<Runnable>();
	private int nextSensorToRead;

	private Integer latestDistanceValue = 0;
	private Integer latestBWFValue = 0;

	public SensorReader(final ComStream comStream) {
		this.comStream = comStream;
		sensors.add(new Runnable() {

			@Override
			public void run() {
				requestSensor(ComStream.RANGE_SENSOR);
				readSensor(latestDistanceValue);
			}
		});
//		sensors.add(new Runnable() {
//
//			@Override
//			public void run() {
//				requestSensor(ComStream.BWF_SENSOR_LEFT);
//				readSensor(latestBWFValue);
//			}
//		});
		//... etc
	}

	@Override
	public void run() {
		sensors.get(nextSensorToRead++ % sensors.size()).run();
	}
	
	private void requestSensor(byte sensor) {
		try {
			comStream.sendCommand(ComStream.SENSOR_COMMAND, sensor);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	private void readSensor(Integer variable) {
		try {
			byte[] buffer = new byte[4];
			comStream.read(buffer);
			int val = buffer[2] << 8;
			val += buffer[3];
			latestDistanceValue = val;
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	public int getLatestDistanceValue() {
		return latestDistanceValue;
	}

	public int getLatestBWFValue() {
		return latestBWFValue;
	}
}
