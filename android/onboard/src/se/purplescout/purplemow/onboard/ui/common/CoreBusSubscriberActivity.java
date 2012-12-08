package se.purplescout.purplemow.onboard.ui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.purplescout.purplemow.core.bus.CoreBus;
import se.purplescout.purplemow.core.bus.CoreBusSubscriber;
import se.purplescout.purplemow.core.bus.CoreBusSubscription;
import se.purplescout.purplemow.core.bus.event.CoreEvent;
import se.purplescout.purplemow.core.bus.event.CoreEventHandler;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

public abstract class CoreBusSubscriberActivity extends Activity implements CoreBusSubscriber {

	private Map<CoreEvent.Type<?>, List<?>> eventManager = new HashMap<CoreEvent.Type<?>, List<?>>();
	private List<CoreBusSubscription> subscriptions = new ArrayList<CoreBusSubscription>();
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null && msg.obj instanceof CoreEvent<?>) {
				handleEvent((CoreEvent<?>) msg.obj);
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unsubscribeAll();
	}

	@Override
	public final void queueEvent(CoreEvent<?> event) {
		Message msg = Message.obtain();
		msg.obj = event;
		handler.sendMessage(msg);
	}

	@Override
	public final void queueDelayedEvent(CoreEvent<?> event, long ms) {
		Message msg = Message.obtain();
		msg.obj = event;
		handler.sendMessageDelayed(msg, ms);
	}

	protected final <H extends CoreEventHandler> void subscribe(CoreEvent.Type<H> type, H handler) {
		doAdd(type, handler);
		CoreBusSubscription subscription = CoreBus.getInstance().subscribe(type, this);
		subscriptions.add(subscription);
	}

	protected final <H extends CoreEventHandler> void unsubscribeAll() {
		for (CoreBusSubscription subscription : subscriptions) {
			subscription.unsubscribe();
		}
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
