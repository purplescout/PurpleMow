package se.purplescout.purplemow.core.sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import se.purplescout.purplemow.core.ComStream;
import android.util.Log;
import android.widget.TextView;

public class SensorReader extends TimerTask {

	private ComStream comStream;
	private List<Runnable> sensors = new ArrayList<Runnable>();
	private int nextSensorToRead;

	private Integer latestDistanceValue = 0;

	private byte hiB;
	private byte loB;
	private Integer latestBWFValue = 0;
	private TextView text;

	public SensorReader(final ComStream comStream) {
		this.comStream = comStream;
		this.text = text;
		sensors.add(new Runnable() {

			@Override
			public void run() {
				requestSensor(ComStream.RANGE_SENSOR);
				readSensor(latestDistanceValue);
			}
		});
		// sensors.add(new Runnable() {
		//
		// @Override
		// public void run() {
		// requestSensor(ComStream.BWF_SENSOR_LEFT);
		// readSensor(latestBWFValue);
		// }
		// });
		// ... etc
	}

	@Override
	public void run() {
		sensors.get(nextSensorToRead++ % sensors.size()).run();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			Log.e(this.getClass().getName(), String.format("Värden från läsaren före bitshift\n MSB: %h | LSB: %h", buffer[2], buffer[3]));
			// int val = buffer[2] << 8;
			// val += buffer[3];
			byte hi = buffer[2];
			byte lo = buffer[3];
			this.hiB = hi;
			this.loB = lo;
			int val = composeInt(hi, lo);
			Log.e(this.getClass().getName(), "Transformerat värde: " + val);
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

}
