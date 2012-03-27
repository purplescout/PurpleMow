package se.purplescout.purplemow.core;

import java.io.IOException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

class EventHandler extends Handler {

	private enum State {
		IDLE, MOWING, AVOID_OBSTACLE, BWF
	}

	private State state = State.IDLE;
	private MotorFSM motorFSM;
	private final TextView text;
	State oldState = null;

	EventHandler(MotorFSM motorFSM, TextView text) {
		this.motorFSM = motorFSM;
		this.text = text;

	}

	@Override
	public void handleMessage(Message msg) {
		// Log.d(this.getClass().getName(), "handleMessage, msg = " + msg.what + ", state = " + state);

		if (state != oldState) {
			logToTextView("Nu jäklars " + state.name());
		}
		oldState = state;
		try {
			switch (state) {
			case IDLE:
				if (!motorFSM.handleMessageIdle(msg)) {
					if (msg.what == Event.START.ordinal()) {
						changeState(State.MOWING);
					}
				}
				break;
			case MOWING:
				if (!motorFSM.handleMessageMowing(msg)) {
					if (msg.what == Event.BWF_SENSOR.ordinal()) {
						changeState(State.BWF);
					}
					if (msg.what == Event.DIST_SENSOR.ordinal()) {
						changeState(State.AVOID_OBSTACLE);
					}
				}
				break;
			case AVOID_OBSTACLE:
				if (!motorFSM.handleMessageRangeSensor(msg)) {
					changeState(State.MOWING);
				}
				break;
			case BWF:
				if (!motorFSM.handleMessageBwf(msg)) {
					changeState(State.MOWING);
				}
				break;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void invokeEntryAction() throws IOException {
		switch (state) {
		case IDLE:
			break;
		case MOWING:
			break;
		case AVOID_OBSTACLE:
			break;
		case BWF:
			motorFSM.entryActionBwf();
			break;
		}
	}

	private void changeState(State newState) throws IOException {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
		invokeEntryAction();
	}

	private void logToTextView(final String msg) {
		text.post(new Runnable() {

			@Override
			public void run() {
				text.append(msg + "\n");
			}
		});
	}
}

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
		this.sensorReader = new SensorReader(comStream);
		this.timer = new Timer();
		this.motorFSM = new MotorFSM(sensorReader, comStream, timer);
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
		main.stop();
	}

	/*
	 * public void start() { isRunning = true; sensorReader = new SensorReader(comStream);
	 *
	 * motorFSM = new MotorFSM(comStream, eventQueue, motorFSMQueue); main = new Thread(null, this, "PurpleMow"); Timer
	 * sensorReaderTimer = new Timer(); sensorReaderTimer.schedule(sensorReader, 100, 100); motorFSM.start();
	 * main.start(); eventQueue.put(new Event(EventType.START)); // To find the start of the application in the log
	 * Log.e(this.getClass().getName(),
	 * "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!####################################################################!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
	 * ); }
	 *
	 * public void stop() { isRunning = false; sensorReader.cancel(); motorFSM.cancel(); }
	 *
	 * @Override public void run() { State oldState = State.UNKNOWN; int oldDistance = 9999; Random randoimizer = new
	 * Random(); while (isRunning) { try { Event event;
	 *
	 * // Debug-printa lite dynga om avstånd eller status har ändrats if (!state.equals(oldState)) {
	 * logToTextView("State is " + state); } oldState = state;
	 *
	 * // Printa bara om diffen är tillräckligt stor int diff = oldDistance - sensorReader.getLatestDistanceValue(); if
	 * (Math.abs(diff) > 10) { String msg = String.format("Avstånd %d", sensorReader.getLatestDistanceValue());
	 * logToTextView(msg); oldDistance = sensorReader.getLatestDistanceValue(); } switch (state) { case IDLE: //
	 * logToTextView("Doing IDLE"); event = eventQueue.take(); if (event.type == EventType.START) {
	 * motorFSMQueue.add(new Event(EventType.MOVE_FORWARD)); Log.i(this.getClass().getName(), "Enter MOWING state");
	 * changeState(State.MOWING); } break; case MOWING: // logToTextView("Doing MOWING"); int val =
	 * sensorReader.getLatestDistanceValue(); if (val > 420) { // Avoid left or right? Dunno yet since there is only one
	 * sensor. Do it randomly for now. boolean right = randoimizer.nextBoolean(); if (right) {
	 * logToTextView("Avoiding obstacle right"); motorFSMQueue.add(new Event(EventType.AVOID_OBSTACLE_RIGHT)); } else {
	 * logToTextView("Avoiding obstacle left"); motorFSMQueue.add(new Event(EventType.AVOID_OBSTACLE_LEFT)); }
	 * Log.i(this.getClass().getName(), "Enter AVOIDING_OBSTACLE state"); changeState(State.AVOIDING_OBSTACLE); } break;
	 * case AVOIDING_OBSTACLE: // logToTextView("Doing AVOID_OBSTACLE"); event = eventQueue.take(); if (event.type ==
	 * EventType.AVOIDING_OBSTACLE_DONE) { motorFSMQueue.add(new Event(EventType.MOVE_FORWARD));
	 * Log.i(this.getClass().getName(), "Enter MOWING state"); changeState(State.MOWING); } break; default: ; } } catch
	 * (InterruptedException e) { Log.e(this.getClass().getName(), e.getMessage()); e.printStackTrace(); }
	 *
	 * } }
	 *
	 * private void logToTextView(final String msg) {
	 *
	 * text.post(new Runnable() {
	 *
	 * @Override public void run() { text.append(msg + "\n"); } }); }
	 */
	private void changeState(State newState) {
		Log.d(this.getClass().getName(), "Change state from " + state + ", to " + newState);
		state = newState;
	}

}
