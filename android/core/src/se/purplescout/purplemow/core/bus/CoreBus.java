package se.purplescout.purplemow.core.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.purplescout.purplemow.core.bus.event.CoreEvent;
import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public class CoreBus {
	
	private static final CoreBus eventBus = new CoreBus();
	private Map<CoreEvent.Type<?>, List<CoreBusSubscriber>> subscribersMap = Collections.synchronizedMap(new HashMap<CoreEvent.Type<?>, List<CoreBusSubscriber>>());

	public static CoreBus getInstance() {
		return eventBus;
	}

	private CoreBus() {
	}
	
	public <H extends CoreEventHandler> CoreBusSubscription subscribe(CoreEvent.Type<H> evenType, final CoreBusSubscriber subscriber) {
		if (!subscribersMap.containsKey(evenType)) {
			subscribersMap.put(evenType, Collections.synchronizedList(new ArrayList<CoreBusSubscriber>()));
		}
		final List<CoreBusSubscriber> subscribers = subscribersMap.get(evenType);
		subscribers.add(subscriber);
		return new CoreBusSubscription() {
			
			@Override
			public void unsubscribe() {
				subscribers.remove(subscriber);
			}
		};
	}
	
	public void fireEvent(CoreEvent<?> event) {
		List<CoreBusSubscriber> subscribers = subscribersMap.get(event.getType());
		if (subscribers == null) {
			return;
		}
		for (CoreBusSubscriber coreEventConsumer : subscribers) {
			coreEventConsumer.queueEvent(event);
		}
	}
	
	public void fireDelaydEvent(CoreEvent<?> event, long ms) {
		List<CoreBusSubscriber> subscribers = subscribersMap.get(event.getType());
		if (subscribers == null) {
			return;
		}
		for (CoreBusSubscriber coreEventConsumer : subscribers) {
			coreEventConsumer.queueDelayedEvent(event, ms);
		}
	}
}
