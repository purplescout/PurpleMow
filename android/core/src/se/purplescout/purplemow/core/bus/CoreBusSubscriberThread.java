package se.purplescout.purplemow.core.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import se.purplescout.purplemow.core.bus.event.CoreEvent;
import se.purplescout.purplemow.core.bus.event.CoreEventHandler;
import android.util.Log;

public abstract class CoreBusSubscriberThread extends Thread implements CoreBusSubscriber {

	private Map<CoreEvent.Type<?>, List<?>> eventManager = new HashMap<CoreEvent.Type<?>, List<?>>();
	private PriorityBlockingQueue<CoreEvent<?>> queue = new PriorityBlockingQueue<CoreEvent<?>>();
	private List<CoreBusSubscription> subscriptions = new ArrayList<CoreBusSubscription>();
	private boolean isRunning = true;
	private boolean cancelDelayedTasks = false;

	@Override
	public final void run() {
		Log.d(getClass().getSimpleName(), getClass().getSimpleName() + " I'm on the job");
		try {
			while (isRunning) {
				CoreEvent<?> event = queue.take();
				handleEvent(event);
			}
		} catch (InterruptedException e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
	}

	public void shutdown() {
		for (CoreBusSubscription subscription : subscriptions) {
			subscription.unsubscribe();
		}
		isRunning = false;
	}

	@Override
	public final void queueEvent(CoreEvent<?> event) {
		queue.add(event);
	}

	@Override
	public final void queueDelayedEvent(final CoreEvent<?> event, long ms) {
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				if(cancelDelayedTasks) {
					timer.cancel();
					timer.purge();
				} else {
					queue.add(event);
				}
			}
		};
		timer.schedule(task, ms);
	}

	@Override
	public void removeDelayedEvents() {
		cancelDelayedTasks = true;
	}

	@Override
	public void enableDelayedEvents() {
		cancelDelayedTasks = false;
	}
	
	@Override
	public boolean getDelayedEventsStatus() {
		return cancelDelayedTasks;
	}
	
	protected final <H extends CoreEventHandler> void subscribe(CoreEvent.Type<H> type, H handler) {
		doAdd(type, handler);
		CoreBusSubscription subscription = CoreBus.getInstance().subscribe(type, this);
		subscriptions.add(subscription);
	}

	private <H extends CoreEventHandler> void doAdd(CoreEvent.Type<H> type, final H handler){
		if (!eventManager.containsKey(type)) {
			eventManager.put(type, new ArrayList<H>());
		}
		@SuppressWarnings("unchecked")
		final List<H> handlers = (List<H>) eventManager.get(type);
		handlers.add(handler);
	}

	private <H extends CoreEventHandler> void handleEvent(CoreEvent<H> event) {
		@SuppressWarnings("unchecked")
		List<H> handlers = (List<H>) eventManager.get(event.getType());
		if (handlers == null) {
			return;
		}

		for(H handler : handlers){
			event.dispatch(handler);
		}
	}
}
