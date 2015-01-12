package se.purplescout.purplemow.core.fsm.mower.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;

public class ChargingDetectedEvent extends CoreEvent<ChargingDetectedEventHandler> {

	public static final CoreEvent.Type<ChargingDetectedEventHandler> TYPE = new Type<ChargingDetectedEventHandler>();

	@Override
	public void dispatch(ChargingDetectedEventHandler handler) {
		handler.onChargingDetectedEvetent(this);
	}

	@Override
	public CoreEvent.Type<ChargingDetectedEventHandler> getType() {
		return TYPE;
	}
}
