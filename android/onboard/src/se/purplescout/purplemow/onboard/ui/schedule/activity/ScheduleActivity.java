package se.purplescout.purplemow.onboard.ui.schedule.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.activity.RoboFragmentActivity;
import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.backend.service.schedule.ScheduleService;
import se.purplescout.purplemow.onboard.shared.schedule.dto.ScheduleEventDTO;
import se.purplescout.purplemow.onboard.ui.schedule.view.ScheduleListView;
import se.purplescout.purplemow.onboard.ui.schedule.view.ScheduleView;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.inject.Inject;

public class ScheduleActivity extends RoboFragmentActivity {

	private static final boolean _24H_DAY = true;
	private static DateFormat dayFormat = new SimpleDateFormat("EEEE");
	private static DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	public interface ViewDisplay {

		ListView getListView();
	}

	public interface ListViewDisplay {

		TextView getDayText();

		Button getFromBtn();

		Button getToBtn();

		CheckBox getCheckbox();

		View getView();
	}
	
	@Inject ScheduleService scheduleService;

	ViewDisplay display;
	ArrayAdapter<ScheduleEventDTO> adapter;
	LayoutInflater layoutInflater;
	List<ScheduleEventDTO> scheduleEvents = new ArrayList<ScheduleEventDTO>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		display = new ScheduleView(this);
		layoutInflater = LayoutInflater.from(this);
		bind();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_schedule, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			save();
			return true;
		case R.id.reset:
			fetchData();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void bind() {
		adapter = new ArrayAdapter<ScheduleEventDTO>(this, -1) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return createListElement(position, convertView);
			}
		};
		display.getListView().setAdapter(adapter);
		fetchData();
	}
	
	private void fetchData() {
		new AsyncTask<Void, Void, List<ScheduleEventDTO>>() {

			@Override
			protected List<ScheduleEventDTO> doInBackground(Void... params) {
				return scheduleService.getScheduleForWeek(new Date());

			}

			@Override
			protected void onPostExecute(List<ScheduleEventDTO> result) {
				scheduleEvents.clear();
				scheduleEvents.addAll(result);
				updateView();
			}
		}.execute();
	}

	private void updateView() {
		adapter.clear();
		for (ScheduleEventDTO scheduleEvent : scheduleEvents) {
			adapter.add(scheduleEvent);
		}
		adapter.notifyDataSetChanged();
	}
	
	private View createListElement(int position, View convertView) {
		ScheduleEventDTO scheduleEvent = scheduleEvents.get(position);
		ScheduleListView listDisplay = new ScheduleListView(layoutInflater, null);
		String day = dayFormat.format(scheduleEvent.getStartDate());
		listDisplay.getDayText().setText(day);
		listDisplay.getFromBtn().setText(timeFormat.format(scheduleEvent.getStartDate()));
		listDisplay.getToBtn().setText(timeFormat.format(scheduleEvent.getStopDate()));
		listDisplay.getCheckbox().setChecked(scheduleEvent.isActive());
		bind(scheduleEvent, listDisplay);
		
		return listDisplay.getView();
	}

	private void bind(final ScheduleEventDTO scheduleEvent, ScheduleListView listDisplay) {
		final Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(scheduleEvent.getStartDate());
		listDisplay.getFromBtn().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new TimePickerDialog(ScheduleActivity.this, new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						fromCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						fromCal.set(Calendar.MINUTE, minute);
						scheduleEvent.setStartDate(fromCal.getTime());
						if (scheduleEvent.getStopDate().before(fromCal.getTime())) {
							scheduleEvent.setStopDate(fromCal.getTime());
						}
						scheduleEvent.setChanged(true);
						updateView();
					}
				}, fromCal.get(Calendar.HOUR_OF_DAY), fromCal.get(Calendar.MINUTE), _24H_DAY).show();
			}
		});
		
		final Calendar toCal = Calendar.getInstance();
		toCal.setTime(scheduleEvent.getStopDate());
		listDisplay.getToBtn().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new TimePickerDialog(ScheduleActivity.this, new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						toCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						toCal.set(Calendar.MINUTE, minute);
						scheduleEvent.setStopDate(toCal.getTime());
						if (scheduleEvent.getStartDate().after(fromCal.getTime())) {
							scheduleEvent.setStartDate(fromCal.getTime());
						}
						scheduleEvent.setChanged(true);
						updateView();
					}
				}, toCal.get(Calendar.HOUR_OF_DAY), toCal.get(Calendar.MINUTE), _24H_DAY).show();
			}
		});
		
		listDisplay.getCheckbox().setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				scheduleEvent.setActive(isChecked);
				scheduleEvent.setChanged(true);
				updateView();
			}
		});
	}
	
	private void save() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				scheduleService.save(scheduleEvents);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				fetchData();
			}
		}.execute();
	}
}
