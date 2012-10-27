package se.purplescout.purplemow.webapp.client.log.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatDTO;
import se.purplescout.purplemow.webapp.client.log.presenter.LogcatPresenter;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public class LogcatView extends Composite implements LogcatPresenter.View {

	public interface LogViewUIBinder extends UiBinder<HTMLPanel, LogcatView> {
	}

	static class LogcatCellCreator extends HTML {

		private static Template TEMPLATE = GWT.create(Template.class);
		private static Map<String, String> colorMapping = new HashMap<String, String>();
		static {
			colorMapping.put("I", "green");
			colorMapping.put("D", "blue");
			colorMapping.put("W", "orange");
			colorMapping.put("E", "red");
		}

		interface Template extends SafeHtmlTemplates {

			@Template("<div style=\"{0}\">{1}</div>")
			SafeHtml get(SafeStyles style, String entry);
		}

		public static SafeHtml getAsSafeHtml(LogcatDTO dto) {
			String color = colorMapping.get(dto.getPriorityConstant());
			SafeStyles style = SafeStylesUtils.fromTrustedString("color: " + (color != null ? color : "black") + ";");
			return TEMPLATE.get(style, dto.getEntry());
		}
	}

	LogViewUIBinder uiBinder = GWT.create(LogViewUIBinder.class);

	ListDataProvider<LogcatDTO> dataProvider = new ListDataProvider<LogcatDTO>();
	
	@UiField(provided = true) DataGrid<LogcatDTO> dataGrid;
	@UiField TextBox filters;
	@UiField Label errorMsg;

	public LogcatView() {
		dataGrid = new DataGrid<LogcatDTO>();
		dataProvider.addDataDisplay(dataGrid);
		setupColumns();
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setLogcat(List<LogcatDTO> logcatDTOs) {
		if (logcatDTOs.isEmpty()) {
			return;
		}
		dataGrid.setPageSize(logcatDTOs.size());
		dataProvider.getList().clear();
		dataProvider.getList().addAll(logcatDTOs);
		dataProvider.flush();
		dataGrid.getRowElement(dataGrid.getRowCount() - 1).scrollIntoView();
	}
	
	@Override
	public TextBox getFilters() {
		return filters;
	}

	@Override
	public void showParseError(boolean visible) {
		errorMsg.setVisible(visible);
	}

	private void setupColumns() {
		AbstractCell<LogcatDTO> cell = new AbstractCell<LogcatDTO>() {

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, LogcatDTO value, SafeHtmlBuilder sb) {
				sb.append(LogcatCellCreator.getAsSafeHtml(value));
			}
		};
		Column<LogcatDTO, LogcatDTO> entry = new Column<LogcatDTO, LogcatDTO>(cell) {

			@Override
			public LogcatDTO getValue(LogcatDTO object) {
				return object;
			}
		};
		dataGrid.addColumn(entry);
	}
}
