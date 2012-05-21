package se.purplescout.purplemow.core;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class SensorReader implements Runnable {

	private ComStream comStream;
	private Handler messageQueue;

	private Integer latestDistanceValue = 0;

	private byte hiB;
	private byte loB;
	private Integer latestBWFValue = 0;
	private final TextView textView;
	private boolean isRunning = true;

	public SensorReader(ComStream comStream, TextView textView) {
		this.comStream = comStream;
		this.textView = textView;

	}

	public void start() {
		Thread thread = new Thread(null, this, "SensorReader");
		thread.start();
		logToTextView("SensorReader started");
	}

	public void connect(Handler handler) {
		messageQueue = handler;
	}

	@Override
	public void run() {
		logToTextView("SensorReader running");
		while (isRunning()) {
			readSensor();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
			logToTextView("Entering readSensor()");
			byte hi = buffer[2];
			byte lo = buffer[3];
			this.hiB = hi;
			this.loB = lo;
			int val = composeInt(hi, lo);
			latestDistanceValue = val;
			logToTextView("Sensor data read: " + val + ". Bytes are: " + buffer[0] + ", " + buffer[1] + ", " + buffer[2] + ", " + buffer[3] + ", ");
			Message msg = messageQueue.obtainMessage(buffer[1], val, 0);
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

	private void logToTextView(final String msg) {
		textView.post(new Runnable() {

			@Override
			public void run() {
				CharSequence fromTextView = textView.getText();
				fromTextView = msg + "\n" + fromTextView;
				textView.setText(fromTextView);
			}
		});
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

}
