package se.purplescout.purplemow.webapp.client.remote.presenter;

import se.purplescout.purplemow.webapp.client.AbstractCallback;
import se.purplescout.purplemow.webapp.client.remote.service.RemoteService;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
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

	public enum Direction {
		FORWARD, REVERSE, LEFT, RIGHT
	}

	View view;
	RemoteService service;

	public RemotePresenter(View view, RemoteService service) {
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
		service.decrementCutterSpeed(new AbstractCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}
		});
	}

	private void incrementCutterSpeed() {
		service.incrementCutterSpeed(new AbstractCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}
		});
	}

	private void incrementMovementSpeed(final Direction direction) {
		service.incrementMovementSpeed(direction, new AbstractCallback<Void>() {

			@Override
			public void onSuccess(Void result) {
			}
		});
	}

	private void stop() {
		service.stopMovment(new AbstractCallback<Void>(){

			@Override
			public void onSuccess(Void result) {
			}
		});
	}
}
