package se.purplescout.purplemow.core.bus;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public interface CoreBusSubscriber {

	void queueEvent(CoreEvent<?> event);

	void queueDelayedEvent(CoreEvent<?> event, long ms);
}
