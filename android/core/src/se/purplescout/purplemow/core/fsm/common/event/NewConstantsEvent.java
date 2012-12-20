package se.purplescout.purplemow.core.fsm.common.event;

import se.purplescout.purplemow.core.bus.event.CoreEvent;
import se.purplescout.purplemow.core.common.Constants;

public class NewConstantsEvent extends CoreEvent<NewConstantsEventHandler> {

	public static final CoreEvent.Type<NewConstantsEventHandler> TYPE = new Type<NewConstantsEventHandler>();
	
	private final Constants constants;
	
	public NewConstantsEvent(Constants constants) {
		this.constants = constants;
	}

	@Override
	public void dispatch(NewConstantsEventHandler handler) {
		handler.onNewConstants(this);
	}

	@Override
	public CoreEvent.Type<NewConstantsEventHandler> getType() {
		return TYPE;
	}

	public Constants getConstants() {
		return constants;
	}
}
