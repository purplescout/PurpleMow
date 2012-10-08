package se.purplescout.purplemow.webapp.client;

import se.purplescout.purplemow.webapp.client.remote.place.RemotePlace;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class PurpleMowEntryPoint extends Composite implements EntryPoint {

	public static final String RPC_ROOT = "purplemow/rpc";
	private static final String DIV_ID = "gwt_placeHolder";

	@UiTemplate("PurpleMowView.ui.xml")
	public interface PurpleMowViewUIBinder extends UiBinder<HTMLPanel, PurpleMowEntryPoint> {
	}

	PurpleMowViewUIBinder uiBinder = GWT.create(PurpleMowViewUIBinder.class);

	@UiField SimplePanel menu;
	@UiField SimplePanel center;

	public PurpleMowEntryPoint() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void onModuleLoad() {
		SimpleEventBus eventBus = GWT.create(SimpleEventBus.class);
		PlaceController placeController = new PlaceController(eventBus);
		PurpleMowPlaceHistoryMapper historyMapper = GWT.create(PurpleMowPlaceHistoryMapper.class);
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		
		ActivityMapper menuActivityMapper = new MenuActivityMapper(historyMapper);
		ActivityMapper centerActivityMapper = new CenterActivityMapper();

		ActivityManager menuActivityManager = new ActivityManager(menuActivityMapper, eventBus);
		ActivityManager centerActivityManager = new ActivityManager(centerActivityMapper, eventBus);

		menuActivityManager.setDisplay(menu);
		centerActivityManager.setDisplay(center);
		
		historyHandler.register(placeController, eventBus, new RemotePlace());

		RootPanel.get(DIV_ID).add(this);
		historyHandler.handleCurrentHistory();
	}
}
