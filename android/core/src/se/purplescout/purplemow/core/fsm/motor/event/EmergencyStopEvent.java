package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class EmergencyStopEvent extends CoreEvent<EmergencyStopEventHandler> {

	public static final CoreEvent.Type<EmergencyStopEventHandler> TYPE = new Type<EmergencyStopEventHandler>();

	@Override
	public void dispatch(EmergencyStopEventHandler handler) {
		handler.onEmergencyStop(this);
	}

	@Override
	public CoreEvent.Type<EmergencyStopEventHandler> getType() {
		return TYPE;
	}
}
