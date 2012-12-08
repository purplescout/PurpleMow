package se.purplescout.purplemow.core.fsm.motor.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface EmergencyStopEventHandler extends CoreEventHandler {

	void onEmergencyStop(EmergencyStopEvent event);
}
