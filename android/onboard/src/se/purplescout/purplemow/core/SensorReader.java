package se.purplescout.purplemow.core;

import java.io.IOException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SensorReader implements Runnable {

	private ComStream comStream;
	private Handler messageQueue;

	private Integer latestDistanceValue = 0;

	private byte hiB;
	private byte loB;
	private Integer latestBWFValue = 0;
	
	public SensorReader(ComStream comStream) {
		this.comStream = comStream;
	}

	public void start() {
		Thread thread = new Thread(null, this, "SensorReader");
		thread.start();	
	}

	public void connect(Handler handler) {
		messageQueue = handler;
	}

	@Override
	public void run() {
		while (true) {
			readSensor();
		}
	}

	void requestSensor(byte sensor) {
		try {
			comStream.sendCommand(ComStream.SENSOR_COMMAND, sensor);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}

	private void readSensor() {
		try {
			byte[] buffer = new byte[4];
			comStream.read(buffer);
//			Log.e(this.getClass().getName(), String.format("Värden från läsaren före bitshift\n MSB: %h | LSB: %h", buffer[2], buffer[3]));
			// int val = buffer[2] << 8;
			// val += buffer[3];
			byte hi = buffer[2];
			byte lo = buffer[3];
			this.hiB = hi;
			this.loB = lo;
			int val = composeInt(hi, lo);
//			Log.e(this.getClass().getName(), "Transformerat värde: " + val);
			latestDistanceValue = val;

			Message msg = messageQueue.obtainMessage(buffer[0], val, 0);
			messageQueue.sendMessageDelayed(msg, 100);
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
