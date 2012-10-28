package se.purplescout.purplemow.webapp.client.log.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatDTO;
import se.purplescout.purplemow.webapp.client.log.presenter.LogPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class LogView extends Composite implements LogPresenter.View {

	public interface LogViewUIBinder extends UiBinder<HTMLPanel, LogView> {
	}

	static class LogcatEntry extends HTML {

		private static Template TEMPLATE = GWT.create(Template.class);
		private static Map<String, String> colorMapping = new HashMap<String, String>();
		static {
			colorMapping.put("I", "blue");
			colorMapping.put("D", "green");
			colorMapping.put("W", "orange");
			colorMapping.put("E", "red");
		}

		interface Template extends SafeHtmlTemplates {

			@Template("<span style=\"{0}\">{1}</span>")
			SafeHtml get(SafeStyles style, String entry);
		}

		public LogcatEntry(LogcatDTO dto) {
			String color = colorMapping.get(dto.getPriorityConstant());
			SafeStyles style = SafeStylesUtils.fromTrustedString("color: " + (color != null ? color : "black") + ";");
			setHTML(TEMPLATE.get(style, dto.getEntry()));
		}
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
