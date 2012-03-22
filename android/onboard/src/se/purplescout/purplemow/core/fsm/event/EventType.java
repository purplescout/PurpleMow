package se.purplescout.purplemow.core.fsm.event;

public enum EventType {

	/**
	 * Main FSM events
	 */

	START, AVOIDING_OBSTACLE_DONE, NEW_SENSOR_DATA,

	/**
	 * Motor FSM events
	 */
	MOVE_FORWARD, MOVE_BACKWARD, TURN_LEFT, TURN_RIGHT, STOP, AVOID_OBSTACLE_LEFT, AVOID_OBSTACLE_RIGHT
}
