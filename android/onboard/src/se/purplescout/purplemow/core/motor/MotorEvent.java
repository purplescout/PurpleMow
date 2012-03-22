package se.purplescout.purplemow.core.motor;

@Deprecated
public enum MotorEvent {
	MOVE_FORWARD, 
	STOP, 
	MOVE_BACKWARD, 
	TURN_LEFT, 
	TURN_RIGHT, 
	AVOID_OBSTACLE_LEFT, 
	AVOID_OBSTACLE_RIGHT,
	
	AVOID_OBSTACLE_LEFT_DONE,
	AVOID_OBSTACLE_RIGHT_DONE
}
