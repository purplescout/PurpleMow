package se.purplescout.purplemow.webapp.client.remote.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;

public class RemotePresenter extends AbstractActivity {
	public interface View extends IsWidget {

		Button getReverseButton();

		Button getLeftButton();

		Button getRightButton();

		Button getForwardButton();

		Button getStopButton();

		Button getCutterOnButton();

		Button getCutterOfButton();
	}

	public interface Service {

		void incrementMovementSpeed(Direction direction, AsyncCallback<Void> callback);

		void incrementCutterSpeed(AsyncCallback<Void> callback);
		
		void decrementCutterSpeed(AsyncCallback<Void> callback);

		void stopMovement(AsyncCallback<Void> callback);
	}

	public enum Direction {
		FORWARD, REVERSE, LEFT, RIGHT
	}

	View view;
	Service service;

	public RemotePresenter(View view, Service service) {
		this.view = view;
		this.service = service;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
		bind();
	}

	private void bind() {
		view.getCutterOfButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				decrementCutter();
			}
		});

		view.getCutterOnButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				incrementCutterSpeed();
			}
		});

		view.getForwardButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				incrementMovementSpeed(Direction.FORWARD);
			}
		});

		view.getLeftButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				incrementMovementSpeed(Direction.LEFT);
			}
		});

		view.getReverseButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				incrementMovementSpeed(Direction.REVERSE);
			}
		});

		view.getRightButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				incrementMovementSpeed(Direction.RIGHT);
			}
		});

		view.getStopButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				stop();
			}
		});
	}

	private void decrementCutter() {
		service.decrementCutterSpeed(new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}

	private void incrementCutterSpeed() {
		service.incrementCutterSpeed(new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}

	private void incrementMovementSpeed(final Direction direction) {
		service.incrementMovementSpeed(direction, new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}

	private void stop() {
		service.stopMovement(new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
			}
		});
	}
}
