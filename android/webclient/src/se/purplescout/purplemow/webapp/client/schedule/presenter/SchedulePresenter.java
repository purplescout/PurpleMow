package se.purplescout.purplemow.webapp.client.schedule.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.dto.ScheduleEventDTO;
import se.purplescout.purplemow.webapp.client.AbstractCallback;
import se.purplescout.purplemow.webapp.client.schedule.model.ScheduleEntry;
import se.purplescout.purplemow.webapp.client.schedule.service.ScheduleService;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class SchedulePresenter extends AbstractActivity {

	private final DateTimeFormat dateFormat = DateTimeFormat.getFormat(LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().dateTime("", "MM/dd"));

	public interface View extends IsWidget {

		Button getNextButton();

		Button getPrevButton();

		void setupTable(List<String> headerDates, List<String> hours);

		void addScheduleEntry(ScheduleEntry entry);
	}

	public enum ViewInterval {
		WEEKLY
	}

	View view;
	ScheduleService service;
	SimpleEventBus eventBus;

	final Date date = new Date();
	final List<ScheduleEventDTO> scheduleEventDTOs = new ArrayList<ScheduleEventDTO>();
	final List<Date> headerDates = new ArrayList<Date>();

	public SchedulePresenter(View view, ScheduleService service, SimpleEventBus eventBus) {
		this.view = view;
		this.service = service;
		this.eventBus = eventBus;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		bind();
		fetchWeekSchedule();
		panel.setWidget(view);
	}

	private void bind() {
		view.getNextButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CalendarUtil.addDaysToDate(date, 7);
				fetchWeekSchedule();
			}
		});

		view.getPrevButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CalendarUtil.addDaysToDate(date, -7);
				fetchWeekSchedule();
			}
		});
	}

	private void fetchWeekSchedule() {
		service.getDatesForWeek(date, new AbstractCallback<List<Date>>() {

			@Override
			public void onSuccess(List<Date> result) {
				headerDates.clear();
				headerDates.addAll(result);
				setupWeekSchedule();
				service.getScheduleForWeek(date, new AbstractCallback<List<ScheduleEventDTO>>() {

					@Override
					public void onSuccess(List<ScheduleEventDTO> result) {
						scheduleEventDTOs.clear();
						scheduleEventDTOs.addAll(result);
						addWeekScheduleEntries();
					}
				});
			}
		});
	}

	private void setupWeekSchedule() {
		List<String> hours = getHours();
		List<String> weekDays = getWeekdays();
		List<String> headerCols = new ArrayList<String>();
		for (int day = 0; day < 7; day++) {
			headerCols.add(weekDays.get(day) + " " + dateFormat.format(headerDates.get(day)));
		}
		view.setupTable(headerCols, hours);
	}

	private void addWeekScheduleEntries() {
		List<ScheduleEntry> scheduleEntries = new ArrayList<ScheduleEntry>();
		for (ScheduleEventDTO dto : scheduleEventDTOs) {
			List<ScheduleEntry> entries = new ScheduleEntry.Builder(dto).build();
			scheduleEntries.addAll(entries);
		}

		for (ScheduleEntry entry : scheduleEntries) {
			view.addScheduleEntry(entry);
		}
	}

	private List<String> getWeekdays() {
		List<String> weekDays = new ArrayList<String>(Arrays.asList(LocaleInfo.getCurrentLocale().getDateTimeFormatInfo().weekdaysShort()));
		String sunday = weekDays.remove(0);
		weekDays.add(6, sunday);

		return weekDays;
	}

	private List<String> getHours() {
		List<String> hours = new ArrayList<String>();
		for (int hour = 0; hour < 24 ; hour++) {
			if (hour < 10) {
				hours.add("0" + hour + ":00");
			} else {
				hours.add(hour + ":00");
			}
		}

		return hours;
	}
}
