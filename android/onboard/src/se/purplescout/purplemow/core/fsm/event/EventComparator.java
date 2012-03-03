package se.purplescout.purplemow.core.fsm.event;

import java.util.Comparator;

public class EventComparator implements Comparator<Event> {

	@Override
	public int compare(Event left, Event right) {
		return left.prio.compareTo(right.prio);
	}

}
