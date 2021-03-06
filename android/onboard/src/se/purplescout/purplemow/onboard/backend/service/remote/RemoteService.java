package se.purplescout.purplemow.onboard.backend.service.remote;


public interface RemoteService {
	public enum Direction {
		FORWARD, REVERSE, LEFT, RIGHT
	}

	void incrementMovmentSpeed(Direction direction);

	void stop();

	void incrementCutterSpeed();

	void decrementCutterSpeed();
}
