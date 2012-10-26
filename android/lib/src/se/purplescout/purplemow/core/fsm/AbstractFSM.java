package se.purplescout.purplemow.core.fsm;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import se.purplescout.purplemow.core.fsm.event.FSMEvent;

import android.util.Log;

public abstract class AbstractFSM<T extends FSMEvent<?, T>> extends Thread {

	private PriorityBlockingQueue<T> queue = new PriorityBlockingQueue<T>();
	private boolean isRunning = true;

	@Override
	public void run() {
		try {
			while (isRunning) {
				T event;
				event = queue.take();
				handleEvent(event);
			}
		} catch (InterruptedException e) {
			Log.e(getClass().getCanonicalName(), e.getMessage(), e);
		}
	}

	public void shutdown() {
		isRunning = false;
	}

	public void queueEvent(T event) {
		event.setTimeStamp(System.currentTimeMillis());
		queue.add(event);
	}

	public void queueDelayedEvent(final T event, long ms) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				event.setTimeStamp(System.currentTimeMillis());
				queue.add(event);
			}
		};
		new Timer().schedule(task, ms);
	}

	protected abstract void handleEvent(T event);
}
