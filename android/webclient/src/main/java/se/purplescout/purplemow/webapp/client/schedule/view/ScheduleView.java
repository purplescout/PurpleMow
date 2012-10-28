package se.purplescout.purplemow.webapp.client.schedule.view;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.purplescout.purplemow.webapp.client.schedule.presenter.SchedulePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

public class ScheduleView extends Composite implements SchedulePresenter.View {
	
	private final DateTimeFormat dayOfWeekFormat = DateTimeFormat.getFormat("E");
	
	public interface ScheduleViewUIBuilder extends UiBinder<HTMLPanel, ScheduleView> {
	}

	public interface Style extends CssResource {

		String first();

		String day();

		String mowEventTop();

		String mowEventBottom();

		String mowEvent();
		
		String error();
		
		String cell();
	}
	
	private final ScheduleViewUIBuilder uiBuilder = GWT.create(ScheduleViewUIBuilder.class);

	@UiField FlexTable table;
	@UiField Style style;
	@UiField Panel panel;
	@UiField Button reset;
	@UiField Button save;
	
	private final Map<String, Integer> headers = new HashMap<String, Integer>();
	
	public ScheduleView() {
		initWidget(uiBuilder.createAndBindUi(this));
	}

	@Override
	public void setupTable(List<Date> weekDates) {
		headers.clear();
		table.clear();
		for (int i = 0; i < 7; i++) {
			int col = i + 1;
			Date date = weekDates.get(i);
			headers.put(dayOfWeekFormat.format(date), col);
			table.setWidget(0, col, new Label(dayOfWeekFormat.format(date)));
			
			TextBox startTextBox = new TextBox();
			startTextBox.setStyleName(style.cell());
			startTextBox.setEnabled(false);
			table.setWidget(1, col, startTextBox);
			
			TextBox stopTextBox = new TextBox();
			stopTextBox.setStyleName(style.cell());
			stopTextBox.setEnabled(false);
			table.setWidget(2, col, stopTextBox);
			
			CheckBox checkBox = new CheckBox();
			checkBox.setValue(false);
			table.setWidget(3, col, checkBox);
		}
		
		table.setWidget(1, 0, new Label("Start"));
		table.setWidget(2, 0, new Label("Stop"));
	}

	@Override
	public Button getSaveButton() {
		return save;
	}

	@Override
	public Button getResetButton() {
		return reset;
	}

	@Override
	public TextBox getStartTimeBox(Date date) {
		return getTimeBox(date, 1);
	}

	@Override
	public TextBox getStopTimeBox(Date date) {
		return getTimeBox(date, 2);
	}
		
	@Override
	public CheckBox getCheckBox(Date date) {
		int col = headers.get(dayOfWeekFormat.format(date));
		return (CheckBox) table.getWidget(3, col);
	}

	private TextBox getTimeBox(Date date, int row) {
		int col = headers.get(dayOfWeekFormat.format(date));
		return (TextBox) table.getWidget(row, col);
	}

	@Override
	public String getErrorStyle() {
		return style.error();
	}
}
