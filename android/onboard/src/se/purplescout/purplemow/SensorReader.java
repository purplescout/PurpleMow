package se.purplescout.purplemow;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;

public class SensorReader implements Runnable {

	private ComStream comStream;
	private Handler messageQueue;
	
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
		byte[] buffer = new byte[4];
		while (true) {
			try {
				comStream.read(buffer);
				// TODO - update. This is just to make it work with the simulator
				Message msg = messageQueue.obtainMessage(buffer[0], (int) (buffer[3] & 0xFF), 0);
				messageQueue.sendMessageDelayed(msg, 100);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
