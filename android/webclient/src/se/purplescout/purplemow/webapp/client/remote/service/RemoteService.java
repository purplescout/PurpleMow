package se.purplescout.purplemow.webapp.client.remote.service;

import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter;
import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter.Direction;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class RemoteService implements RemotePresenter.Service {

	@Override
	public void incrementMovementSpeed(Direction direction, AsyncCallback<Void> callback) {
		String command = getCommand(direction);
		sendCommand(command, callback);
	}

	@Override
	public void stopMovement(AsyncCallback<Void> callback) {
		sendCommand("stop", callback);
	}
	
	@Override
	public void incrementCutterSpeed(AsyncCallback<Void> callback) {
		sendCommand("cutter_on", callback);
	}

	@Override
	public void decrementCutterSpeed(AsyncCallback<Void> callback) {
		sendCommand("cutter_off", callback);
	}

	private void sendCommand(String command, final AsyncCallback<Void> callback) {
		sendCommand(command, -1, callback);
	}

	private void sendCommand(String command, int value, final AsyncCallback<Void> callback) {
		try {
			RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "command");
			rb.setHeader("Content-type", "application/x-www-form-urlencoded");
			StringBuilder data = new StringBuilder();
			data.append("cmd").append("=").append(command).append("&").append("value").append("=").append(value);
			rb.sendRequest(data.toString(), new RequestCallback() {

				@Override
				public void onResponseReceived(Request request, Response response) {
					callback.onSuccess(null);
				}

				@Override
				public void onError(Request request, Throwable exception) {
					callback.onFailure(exception);
				}
			});

		} catch (RequestException e) {
			callback.onFailure(e);
		}
	}

	private String getCommand(Direction direction) {
		String command = null;
		switch (direction) {
		case FORWARD:
			command = "forward";
			break;
		case REVERSE:
			command = "backward";
			break;
		case LEFT:
			command = "left";
			break;
		case RIGHT:
			command = "right";
			break;
		}

		return command;
	}
}
