package se.purplescout.purplemow.onboard.ui.configure.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.ActivityBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.configure.activity.ConfigureActivity;
import se.purplescout.purplemow.onboard.ui.widget.ValuePickerView;
import android.app.Activity;
import android.widget.EditText;

@ContentView(R.layout.activity_configure)
public class ConfigureView extends ActivityBinderView implements ConfigureActivity.ViewDisplay {

	@UiField(R.id.configFullSpeedText) EditText fullSpeedText;
	@UiField(R.id.configNoSpeedText) EditText noSpeedText;
	@UiField(R.id.configRangeLimitPicker) ValuePickerView ragneLimitPicker;
	@UiField(R.id.configBwfLimitPicker) ValuePickerView bwfLimitPicker;
	@UiField(R.id.configBatteryLowPicker) ValuePickerView batteryLowPicker;
	@UiField(R.id.configBatteryChargedPicker) ValuePickerView batteryChargedPicker;
	@UiField(R.id.configGoHomeOffsetPicker) ValuePickerView goHomeOffsetPicker;
	@UiField(R.id.configGoHomeHysteresPicker) ValuePickerView goHomeHysteresPicker;
	@UiField(R.id.configGoHomeThresholdNegNarrowPicker) ValuePickerView goHomeThresholdNegNarrowPicker;
	@UiField(R.id.configGoHomeThresholdPosNarrowPicker) ValuePickerView goHomeThresholdPosNarrowPicker;
	@UiField(R.id.configGoHomeThresholdNegWidePicker) ValuePickerView goHomeThresholdNegWidePicker;
	@UiField(R.id.configGoHomeThresholdPosWidePicker) ValuePickerView goHomeThresholdPosWidePicker;
	
	public ConfigureView(Activity activity) {
		super(activity);
	}

	@Override
	public EditText getFullSpeedText() {
		return fullSpeedText;
	}

	@Override
	public EditText getNoSpeedText() {
		return noSpeedText;
	}

	@Override
	public ValuePickerView getRangeLimitPicker() {
		return ragneLimitPicker;
	}

	@Override
	public ValuePickerView getBwfLimitPicker() {
		return bwfLimitPicker;
	}

	@Override
	public ValuePickerView getBatteryLowPicker() {
		return batteryLowPicker;
	}

	@Override
	public ValuePickerView getBatteryChargedPicker() {
		return batteryChargedPicker;
	}

	@Override
	public ValuePickerView getGoHomeOffsetPicker() {
		return goHomeOffsetPicker;
	}

	@Override
	public ValuePickerView getGoHomeHysteresPicker() {
		return goHomeHysteresPicker;
	}
	
	@Override
	public ValuePickerView getGoHomeThresholdNegNarrowPicker() {
		return goHomeThresholdNegNarrowPicker;
	}

	@Override
	public ValuePickerView getGoHomeThresholdPosNarrowPicker() {
		return goHomeThresholdPosNarrowPicker;
	}
	
	@Override
	public ValuePickerView getGoHomeThresholdNegWidePicker() {
		return goHomeThresholdNegWidePicker;
	}

	@Override
	public ValuePickerView getGoHomeThresholdPosWidePicker() {
		return goHomeThresholdPosWidePicker;
	}
}
