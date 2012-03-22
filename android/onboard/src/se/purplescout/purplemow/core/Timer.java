package se.purplescout.purplemow.core;

import android.os.Handler;
import android.util.Log;

public class Timer {
	private Handler messageQueue;

	public Timer() {
	}
	
	public void connect(Handler handler) {
		messageQueue = handler;
	}

	public void startTimer(int delayMillis) {
		Log.d(this.getClass().getName(), "Start timer event: " + delayMillis + " ms");
		messageQueue.sendEmptyMessageDelayed(Event.TIMER.ordinal(), delayMillis);
	}

	public void cancelTimer() {
	}
}
