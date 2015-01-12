package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEventHandler;

public interface ChargingDetectedEventHandler extends CoreEventHandler {

	void onChargingDetectedEvetent(ChargingDetectedEvent event);
}
