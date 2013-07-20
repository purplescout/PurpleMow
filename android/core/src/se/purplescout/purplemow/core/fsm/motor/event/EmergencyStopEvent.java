package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class EmergencyStopEvent extends CoreEvent<EmergencyStopEventHandler> {

	public static final CoreEvent.Type<EmergencyStopEventHandler> TYPE = new Type<EmergencyStopEventHandler>();
	private String message;

	public EmergencyStopEvent(String message) {
		this.message = message;
	}

	@Override
	public void dispatch(EmergencyStopEventHandler handler) {
		handler.onEmergencyStop(this);
	}

	@Override
	public CoreEvent.Type<EmergencyStopEventHandler> getType() {
		return TYPE;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
