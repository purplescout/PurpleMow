package se.purplescout.purplemow.webapp.client.schedule.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import se.purplescout.purplemow.webapp.client.AbstractCallback;
import se.purplescout.purplemow.webapp.client.schedule.service.ScheduleService;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

public class SchedulePresenter extends AbstractActivity {

	private final DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm");

	public interface View extends IsWidget {

		void setupTable(List<Date> weekDates);

		TextBox getStartTimeBox(Date date);

		TextBox getStopTimeBox(Date date);

		Button getSaveButton();

		Button getResetButton();
		
		String getErrorStyle();

		CheckBox getCheckBox(Date startDate);
	}

	public interface OnScheduleEventChange {

		void onChange(ScheduleEventDTO dto);
	}

	private abstract class TimeChangeHandler implements ValueChangeHandler<String> {

		protected final ScheduleEventDTO dto;
		private final TextBox textBox;

		public TimeChangeHandler(ScheduleEventDTO dto, TextBox textBox) {
			this.dto = dto;
			this.textBox = textBox;
		}

		@Override
		public void onValueChange(ValueChangeEvent<String> event) {
			try {
				Date date = timeFormat.parse(event.getValue());
				setTime(date);
				dto.setChanged(true);	
				textBox.setText(timeFormat.format(date));
				textBox.removeStyleName(view.getErrorStyle());
			} catch (IllegalArgumentException e) {
				textBox.addStyleName(view.getErrorStyle());
			} finally {
				view.getSaveButton().setEnabled(validate());
				view.getResetButton().setEnabled(true);
			}
		}
		
		protected abstract void setTime(Date date);
	}

	private class StartTimeChangeHandler extends TimeChangeHandler {

		public StartTimeChangeHandler(ScheduleEventDTO dto, TextBox textBox) {
			super(dto, textBox);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void setTime(Date date) {
			Date startDate = dto.getStartDate();
			startDate.setHours(date.getHours());
			startDate.setMinutes(date.getMinutes());
			if (dto.getStopDate().before(dto.getStartDate())) {
				dto.setStartDate(dto.getStopDate());
			}
		}
	}

	private class StopTimeChangeHandler extends TimeChangeHandler {

		public StopTimeChangeHandler(ScheduleEventDTO dto, TextBox textBox) {
			super(dto, textBox);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void setTime(Date date) {
			Date endDate = dto.getStopDate();
			endDate.setHours(date.getHours());
			endDate.setMinutes(date.getMinutes());
			if (dto.getStopDate().before(dto.getStartDate())) {
				dto.setStopDate(dto.getStartDate());
			}
		}
	}

	public enum ViewInterval {
		WEEKLY
	}

	View view;
	ScheduleService service;

	final Date date = new Date();
	final List<ScheduleEventDTO> scheduleEventDTOs = new ArrayList<ScheduleEventDTO>();
	final List<Date> weekDates = new ArrayList<Date>();

	public SchedulePresenter(View view, ScheduleService service) {
		this.view = view;
		this.service = service;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		bind();
		fetchWeekDates();
		panel.setWidget(view);
	}

	private void bind() {
		view.getResetButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				fetchWeekDates();
			}
		});
		view.getSaveButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				save();
			}
		});
	}

	private void fetchWeekDates() {
		service.getDatesForWeek(date, new AbstractCallback<List<Date>>() {

			@Override
			public void onSuccess(List<Date> response) {
				weekDates.clear();
				weekDates.addAll(response);
				view.setupTable(weekDates);
				fetchScheduleForWeek();
			}
		});
	}

	private void fetchScheduleForWeek() {
		service.getScheduleForWeek(date, new AbstractCallback<List<ScheduleEventDTO>>() {

			@Override
			public void onSuccess(List<ScheduleEventDTO> result) {
				scheduleEventDTOs.clear();
				scheduleEventDTOs.addAll(result);
				addWeekScheduleEntries();
			}
		});
	}

	private void addWeekScheduleEntries() {
		for (final ScheduleEventDTO dto : scheduleEventDTOs) {
			final TextBox start = view.getStartTimeBox(dto.getStartDate());
			final TextBox stop = view.getStopTimeBox(dto.getStartDate());
			CheckBox enabled = view.getCheckBox(dto.getStartDate());

			start.setText(timeFormat.format(dto.getStartDate()));
			start.setEnabled(dto.isActive());
			stop.setText(timeFormat.format(dto.getStopDate()));
			stop.setEnabled(dto.isActive());
			enabled.setValue(dto.isActive());

			start.addValueChangeHandler(new StartTimeChangeHandler(dto, start));
			stop.addValueChangeHandler(new StopTimeChangeHandler(dto, stop));
			enabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					dto.setActive(event.getValue());
					dto.setChanged(true);
					start.setEnabled(event.getValue());
					stop.setEnabled(event.getValue());
					
					view.getSaveButton().setEnabled(validate());
					view.getResetButton().setEnabled(true);
				}
			});
			
			view.getSaveButton().setEnabled(false);
			view.getResetButton().setEnabled(false);
		}
	}
	

	private boolean validate() {
		boolean valid = true;
		for (Date date : weekDates) {
			TextBox start = view.getStartTimeBox(date);
			TextBox stop = view.getStopTimeBox(date);
			CheckBox enabled = view.getCheckBox(date);
			if (!enabled.getValue()) {
				continue;
			}
			if (!isEmpty(start) && hasValidTime(start)) {
				if (isEmpty(stop)) {
					valid = false;
					stop.addStyleName(view.getErrorStyle());
				} else if(!hasValidTime(stop)) {
					valid = false;
				} else {
					stop.removeStyleName(view.getErrorStyle());
				}
			}
			
			if (!isEmpty(stop) && hasValidTime(stop)) {
				if (isEmpty(start)) {
					valid = false;
					start.addStyleName(view.getErrorStyle());
				} else if(!hasValidTime(start)) {
					valid = false;
				} else {
					start.removeStyleName(view.getErrorStyle());
				}
			}
			if (valid) {
				if (start.getText().compareTo(stop.getText()) > -1) {
					valid = false;
					stop.addStyleName(view.getErrorStyle());
				} else {
					stop.removeStyleName(view.getErrorStyle());
				}
			}
		}
		
		return valid;
	}

	private boolean hasValidTime(TextBox start) {
		try {
			timeFormat.parse(start.getText());
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private boolean isEmpty(TextBox start) {
		return start.getText() == null || start.getText().equals("");
	}	

	private void save() {
		List<ScheduleEventDTO> changed = new ArrayList<ScheduleEventDTO>();
		for (ScheduleEventDTO dto : scheduleEventDTOs) {
			if (dto.isChanged()) {
				changed.add(dto);
			}
		}
		if (changed.size() > 0) {
			service.save(changed, new AbstractCallback<Void>(){

				@Override
				public void onSuccess(Void response) {
					fetchWeekDates();
				}
			});
		} else {
			fetchWeekDates();
		}
	}
}
