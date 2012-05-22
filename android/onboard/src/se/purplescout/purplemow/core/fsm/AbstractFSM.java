package se.purplescout.purplemow.core.fsm;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import android.util.Log;

public abstract class AbstractFSM<T extends Comparable<T>> extends Thread {

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
			e.printStackTrace();
		}
	}

	public void shutdown() {
		isRunning = false;
	}

	public void queueEvent(T event) {
		queue.add(event);
	}

	public void queueDelayedEvent(final T event, long ms) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				queue.add(event);
			}
		};
		new Timer().schedule(task, ms);
	}

	protected abstract void handleEvent(T event);
}
