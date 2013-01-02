package se.purplescout.purplemow.onboard.ui.configure.activity;

import roboguice.activity.RoboActivity;
import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.backend.service.constant.ConstantService;
import se.purplescout.purplemow.onboard.shared.constant.dto.ConstantsDTO;
import se.purplescout.purplemow.onboard.ui.common.SimpleTextWatcher;
import se.purplescout.purplemow.onboard.ui.configure.view.ConfigureView;
import se.purplescout.purplemow.onboard.ui.widget.ValuePickerView;
import se.purplescout.purplemow.onboard.ui.widget.ValuePickerView.ValuePickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.inject.Inject;

public class ConfigureActivity extends RoboActivity {

	public interface ViewDisplay {

		EditText getFullSpeedText();

		EditText getNoSpeedText();
		
		ValuePickerView getRangeLimitPicker();
		
		ValuePickerView getBwfLimitPicker();
		
		ValuePickerView getBatteryLowPicker();
		
		ValuePickerView getBatteryChargedPicker();
		
		ValuePickerView getGoHomeOffsetPicker();

		ValuePickerView getGoHomeHysteresPicker();
		
		ValuePickerView getGoHomeThresholdPosNarrowPicker();
		
		ValuePickerView getGoHomeThresholdNegNarrowPicker();
		
		ValuePickerView getGoHomeThresholdPosWidePicker();

		ValuePickerView getGoHomeThresholdNegWidePicker();
	}

	@Inject
	ConstantService constantService;

	ViewDisplay display;
	ConstantsDTO constants = new ConstantsDTO();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		display = new ConfigureView(this);
		bind();
		fetchData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_configure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset:
			fetchData();
			return true;
		case R.id.save:
			save();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void bind() {
		display.getFullSpeedText().addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				constants.setFullSpeed(Integer.parseInt(s.toString().trim()));
				constants.setChanged(true);
			}
		});
		display.getNoSpeedText().addTextChangedListener(new SimpleTextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				constants.setNoSpeed(Integer.parseInt(s.toString().trim()));
				constants.setChanged(true);
			}
		});

		display.getRangeLimitPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setRangeLimit(value);
				constants.setChanged(true);
			}
		});
		display.getBwfLimitPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setBwfLimit(value);
				constants.setChanged(true);
			}
		});
		display.getBatteryLowPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setBatteryLow(value);
				constants.setChanged(true);
			}
		});
		display.getBatteryChargedPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setBatteryCharged(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeOffsetPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeOffset(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeHysteresPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeHysteres(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeThresholdPosNarrowPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeThresholdPosNarrow(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeThresholdNegNarrowPicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeThresholdNegNarrow(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeThresholdPosWidePicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeThresholdPosWide(value);
				constants.setChanged(true);
			}
		});
		display.getGoHomeThresholdNegWidePicker().setOnValueChangeListener(new ValuePickListener() {
			
			@Override
			public void onValuePicked(int value) {
				constants.setGoHomeThresholdNegWide(value);
				constants.setChanged(true);
			}
		});
	}

	private void save() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				constantService.save(constants);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				fetchData();
			}
		}.execute();
	}

	private void fetchData() {
		new AsyncTask<Void, Void, ConstantsDTO>() {

			@Override
			protected ConstantsDTO doInBackground(Void... params) {
				return constantService.getConstantsDTO();
			}

			@Override
			protected void onPostExecute(ConstantsDTO result) {
				constants = result;
				updateView();
			}
		}.execute();
	}

	private void updateView() {
		display.getFullSpeedText().setText(String.format("%d", constants.getFullSpeed()));
		display.getNoSpeedText().setText(String.format("%d", constants.getNoSpeed()));
		display.getRangeLimitPicker().setValue(constants.getRangeLimit());
		display.getBwfLimitPicker().setValue(constants.getBwfLimit());
		display.getBatteryLowPicker().setValue(constants.getBatteryLow());
		display.getBatteryChargedPicker().setValue(constants.getBatteryCharged());
		display.getGoHomeHysteresPicker().setValue(constants.getGoHomeHysteres());
		display.getGoHomeOffsetPicker().setValue(constants.getGoHomeOffset());
		display.getGoHomeThresholdPosNarrowPicker().setValue(constants.getGoHomeThresholdPosNarrow());
		display.getGoHomeThresholdNegNarrowPicker().setValue(constants.getGoHomeThresholdNegNarrow());
		display.getGoHomeThresholdPosWidePicker().setValue(constants.getGoHomeThresholdPosWide());
		display.getGoHomeThresholdNegWidePicker().setValue(constants.getGoHomeThresholdNegWide());
	}
}