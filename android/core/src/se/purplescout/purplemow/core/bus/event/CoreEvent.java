package se.purplescout.purplemow.core.bus.event;

public abstract class CoreEvent <T extends CoreEventHandler> implements Comparable<CoreEvent<T>> {

	private static final int DEFAULT_PRIO = 5;

	public static class Type<T> {
	}

	public abstract void dispatch(T handler);

	public abstract CoreEvent.Type<T> getType();

	protected int getPriority() {
		return DEFAULT_PRIO;
	}

	@Override
	public final int compareTo(CoreEvent<T> other) {
		return Integer.signum(this.getPriority() - other.getPriority());
	}
}
