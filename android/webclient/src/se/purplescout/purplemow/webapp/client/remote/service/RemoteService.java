package se.purplescout.purplemow.webapp.client.remote.service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter.Direction;

@Path("rpc/remote")
public interface RemoteService extends RestService {

	@POST
	@Path("incrementMovementSpeed")
	void incrementMovementSpeed(Direction direction, MethodCallback<Void> callback);

	@POST
	@Path("incrementCutterSpeed")
	void incrementCutterSpeed(MethodCallback<Void> callback);

	@POST
	@Path("decrementCutterSpeed")
	void decrementCutterSpeed(MethodCallback<Void> callback);

	@POST
	@Path("stopMovment")
	void stopMovment(MethodCallback<Void> callback);
}
