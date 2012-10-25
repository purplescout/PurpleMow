package se.purplescout.purplemow.webapp.client.log.presenter;

import se.purplescout.purplemow.webapp.client.log.service.LogService;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;


public class LogPresenter extends AbstractActivity {

	public interface View extends IsWidget {

	}

	View view;
	LogService srv;

	public LogPresenter(View view, LogService srv) {
		this.view = view;
		this.srv = srv;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		panel.setWidget(view);
	}
}
