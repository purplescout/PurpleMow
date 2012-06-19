package se.purplescout.purplemow.webapp.client.remote.view;

import se.purplescout.purplemow.webapp.client.remote.presenter.RemotePresenter;
import se.purplescout.purplemow.webapp.client.resource.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
public class RemoteView extends Composite implements RemotePresenter.View {

	private static RemoteviewUiBinder uiBinder = GWT.create(RemoteviewUiBinder.class);

	interface RemoteviewUiBinder extends UiBinder<Widget, RemoteView> {
	}
	
	interface Style extends CssResource {
		
		String hidden();
		
		String remoteButton();
	}

	@UiField Style style;
	
	@UiField Button forward;
	@UiField Button left;
	@UiField Button stop;
	@UiField Button right;
	@UiField Button reverse;
	@UiField Button cutter_on;
	@UiField Button cutter_of;
	@UiField Button dummy1;
	@UiField Button dummy2;
	@UiField Button dummy3;
	
	public RemoteView() {
		initWidget(uiBinder.createAndBindUi(this));

		forward.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowUp()).getElement());
		forward.addStyleName(style.remoteButton());
		left.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowLeft()).getElement());	
		left.addStyleName(style.remoteButton());
		stop.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowStop()).getElement());	
		stop.addStyleName(style.remoteButton());
		right.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowRight()).getElement());	
		right.addStyleName(style.remoteButton());
		reverse.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowDown()).getElement());	
		reverse.addStyleName(style.remoteButton());
		dummy1.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowDown()).getElement());	
		dummy1.addStyleName(style.remoteButton());
		dummy1.addStyleName(style.hidden());
		dummy2.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowDown()).getElement());	
		dummy2.addStyleName(style.remoteButton());
		dummy2.addStyleName(style.hidden());
		dummy3.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowDown()).getElement());	
		dummy3.addStyleName(style.remoteButton());
		dummy3.addStyleName(style.hidden());
		
		cutter_on.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowPlus()).getElement());	
		cutter_on.addStyleName(style.remoteButton());
		cutter_of.getElement().appendChild(new Image(Resources.RESOURCE.stripArrowMinus()).getElement());	
		cutter_of.addStyleName(style.remoteButton());
	}

	@Override
	public Button getReverseButton() {
		return reverse;
	}

	@Override
	public Button getLeftButton() {
		return left;
	}

	@Override
	public Button getRightButton() {
		return right;
	}

	@Override
	public Button getForwardButton() {
		return forward;
	}

	@Override
	public Button getStopButton() {
		return stop;
	}

	@Override
	public Button getCutterOnButton() {
		return cutter_on;
	}


	@Override
	public Button getCutterOfButton() {
		return cutter_of;
	}
}
