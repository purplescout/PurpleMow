package se.purplescout.purplemow.webapp.client.log.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

import se.purplescout.purplemow.webapp.client.log.presenter.LogPresenter;

public class LogView extends Composite implements LogPresenter.View {

	public interface LogViewUIBinder extends UiBinder<HTMLPanel, LogView> {
	}

	LogViewUIBinder uiBinder = GWT.create(LogViewUIBinder.class);

	@UiField Anchor logcat;
	@UiField Anchor bwfLeft;
	@UiField Anchor bwfRight;
	@UiField Anchor rangeLeft;
	@UiField Anchor rangeRight;

	public LogView() {
		initWidget(uiBinder.createAndBindUi(this));
		logcat.setHref("purplemow/rpc/log/logcat.csv");
		bwfLeft.setHref("purplemow/rpc/log/bwfLeftData.csv");
		bwfRight.setHref("purplemow/rpc/log/bwfRightData.csv");
		rangeLeft.setHref("purplemow/rpc/log/rangeLeftData.csv");
		rangeRight.setHref("purplemow/rpc/log/rangeRightData.csv");
	}
}
