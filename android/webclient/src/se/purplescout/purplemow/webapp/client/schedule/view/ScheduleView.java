package se.purplescout.purplemow.webapp.client.schedule.view;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.purplescout.purplemow.webapp.client.schedule.model.ScheduleEntry;
import se.purplescout.purplemow.webapp.client.schedule.presenter.SchedulePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class ScheduleView extends Composite implements SchedulePresenter.View {
	public interface ScheduleViewUIBuilder extends UiBinder<HTMLPanel, ScheduleView> {
	}

	public interface Style extends CssResource {

		String first();

		String day();

		String mowEventTop();

		String mowEventBottom();

		String mowEvent();
	}

	private final ScheduleViewUIBuilder uiBuilder = GWT.create(ScheduleViewUIBuilder.class);

	@UiField Button next;
	@UiField Button prev;
	@UiField FlexTable table;
	@UiField Style style;
	@UiField Panel panel;

	public ScheduleView() {
		initWidget(uiBuilder.createAndBindUi(this));
	}

	@Override
	public void setupTable(List<String> headerCols, List<String> hours) {

		for (int col = 1; col <= 7; col++) {
			table.setWidget(0, col, new Label(headerCols.get(col - 1)));
		}

		Iterator<String> hoursIterator = hours.iterator();
		for (int row = 1; row <= 48; row++) {
			for (int col = 0; col <= 7; col++) {
				if (col == 0) {
					if (row % 2 == 1) {
						try {
							table.setWidget(row, col, new Label(hoursIterator.next()));
						} catch (NoSuchElementException e) {
							table.setWidget(row, col, new Label("test"));
						}
					}

					table.getCellFormatter().addStyleName(row, col, style.first());
				} else {
					table.getCellFormatter().addStyleName(row, col, style.day());
				}
			}
		}
	}

	@Override
	public Button getNextButton() {
		return next;
	}

	@Override
	public Button getPrevButton() {
		return prev;
	}

	@Override
	public void addScheduleEntry(ScheduleEntry entry) {
		int rowDelta = 1;
		int columnDelta = 1;

		int startRow = entry.getStartRow() + rowDelta;
		int endRow = entry.getEndRow() + rowDelta;
		int column = entry.getColumn() + columnDelta;

		for (int row = startRow; row <= endRow; row++) {
			table.getCellFormatter().addStyleName(row, column, style.mowEvent());
		}

		table.getCellFormatter().addStyleName(startRow, column, style.mowEventTop());
		table.getCellFormatter().addStyleName(endRow, column, style.mowEventBottom());
	}
}
