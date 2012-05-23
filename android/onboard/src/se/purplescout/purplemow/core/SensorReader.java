package se.purplescout.purplemow.core;

import java.io.IOException;

import se.purplescout.purplemow.core.fsm.AbstractFSM;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent;
import se.purplescout.purplemow.core.fsm.event.MainFSMEvent.EventType;
import android.util.Log;
import android.widget.TextView;

public class SensorReader extends Thread {

	private ComStream comStream;

	private Integer latestDistanceValue = 0;

	private byte hiB;
	private byte loB;
	private Integer latestBWFValue = 0;
	private final TextView textView;
	private boolean isRunning = true;

	AbstractFSM<MainFSMEvent> mainFSM;

	public SensorReader(ComStream comStream, TextView textView) {
		this.comStream = comStream;
		this.textView = textView;
	}

	public void setMainFSM(AbstractFSM<MainFSMEvent> fsm) {
		mainFSM = fsm;
	}

	@Override
	public void run() {

		try {
			logToTextView("SensorReader running");
			while (isRunning()) {
				requestSensor(ComStream.RANGE_SENSOR);
				readSensor();

				Thread.sleep(100);

				requestSensor(ComStream.BWF_SENSOR_RIGHT);
				readSensor();

				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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
			byte hi = buffer[2];
			byte lo = buffer[3];
			this.hiB = hi;
			this.loB = lo;
			int val = composeInt(hi, lo);
			latestDistanceValue = val;
			if (buffer[1] == ComStream.RANGE_SENSOR) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.RANGE, val));
			} else if (buffer[1] == ComStream.BWF_SENSOR_RIGHT) {
				mainFSM.queueEvent(new MainFSMEvent(EventType.BWF_RIGHT, val));
			}
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
