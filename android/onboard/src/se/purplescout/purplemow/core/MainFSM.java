package se.purplescout.purplemow.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

public class MainFSM implements Runnable {

	public enum State {
		IDLE, MOWING, AVOIDING_OBSTACLE, BWF, UNKNOWN
	}

	private State state = State.IDLE;
	private SensorReader sensorReader;
	private MotorFSM motorFSM;
	private Thread main;
	private ComStream comStream;
	private Handler handler;
	private boolean isRunning = false;

	TextView text;
	private Timer timer;

	public MainFSM(ComStream comStream, TextView textView) {
		this.sensorReader = new SensorReader(comStream, textView);
		this.timer = new Timer();
		this.motorFSM = new MotorFSM(sensorReader, comStream, timer, textView);
		this.text = textView;
	}

	@Override
	public void run() {
		Looper.prepare();

		handler = new EventHandler(motorFSM, text);
		handler.sendEmptyMessage(Event.START.ordinal());

		timer.connect(handler);
		sensorReader.connect(handler);
		sensorReader.start();

		Looper.loop();
	}

	public void start() {
		isRunning = true;
		main = new Thread(null, this, "PurpleMow");
		main.start();
	}

	public void stop() {
		isRunning = false;
		this.sensorReader.setRunning(isRunning);
		Looper.getMainLooper().quit();
	}

	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}
}
