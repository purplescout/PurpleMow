package se.purplescout.purplemow.onboard.ui.configure.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.ActivityBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.configure.activity.ConfigureActivity;
import android.app.Activity;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

@ContentView(R.layout.activity_configure)
public class ConfigureView extends ActivityBinderView implements ConfigureActivity.ViewDisplay {

	@UiField(R.id.configFullSpeedText) EditText fullSpeedText;
	@UiField(R.id.configNoSpeedText) EditText noSpeedText;
	@UiField(R.id.configRangeLimitText) TextView rangeLimitText;
	@UiField(R.id.configRangeLimitSeekBar) SeekBar rangeLimitSeekBar;
	@UiField(R.id.configBwfLimitText) TextView bwfLimitText;
	@UiField(R.id.configBwfLimitSeekBar) SeekBar bwfLimitSeekBar;
	@UiField(R.id.configBatteryLowText) TextView batteryLowText;
	@UiField(R.id.configBatteryLowSeekBar) SeekBar batteryLowSeekBar;
	@UiField(R.id.configBatteryChargedText) TextView batteryChargedText;
	@UiField(R.id.configBatteryChargedSeekBar) SeekBar batteryChargedSeekBar;
	@UiField(R.id.configGoHomeOffsetText) TextView goHomeOffsetText;
	@UiField(R.id.configGoHomeOffsetSeekBar) SeekBar goHomeOffsetSeekBar;
	@UiField(R.id.configGoHomeHysteresText) TextView goHomeHysteresText;
	@UiField(R.id.configGoHomeHysteresSeekBar) SeekBar goHomeHysteresSeekBar;
	@UiField(R.id.configGoHomeThresholdNegText) TextView goHomeThresholdNegText;
	@UiField(R.id.configGoHomeThresholdNegSeekBar) SeekBar goHomeThresholdNegSeekBar;
	@UiField(R.id.configGoHomeThresholdPosText) TextView goHomeThresholdPosText;
	@UiField(R.id.configGoHomeThresholdPosSeekBar) SeekBar goHomeThresholdPosSeekBar;

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
	public TextView getRangeLimitText() {
		return rangeLimitText;
	}

	@Override
	public SeekBar getRangeLimitSeekBar() {
		return rangeLimitSeekBar;
	}

	@Override
	public TextView getBwfLimitText() {
		return bwfLimitText;
	}

	@Override
	public SeekBar getBwfLimitSeekBar() {
		return bwfLimitSeekBar;
	}

	@Override
	public TextView getBatteryLowText() {
		return batteryLowText;
	}

	@Override
	public SeekBar getBatteryLowSeekBar() {
		return batteryLowSeekBar;
	}

	@Override
	public TextView getBatteryChargedText() {
		return batteryChargedText;
	}

	@Override
	public SeekBar getBatteryChargedSeekBar() {
		return batteryChargedSeekBar;
	}

	@Override
	public TextView getGoHomeOffsetText() {
		return goHomeOffsetText;
	}

	@Override
	public SeekBar getGoHomeOffsetSeekBar() {
		return goHomeOffsetSeekBar;
	}

	@Override
	public TextView getGoHomeHysteresText() {
		return goHomeHysteresText;
	}

	@Override
	public SeekBar getGoHomeHysteresSeekBar() {
		return goHomeHysteresSeekBar;
	}

	@Override
	public TextView getGoHomeThresholdNegText() {
		return goHomeThresholdNegText;
	}

	@Override
	public SeekBar getGoHomeThresholdNegSeekBar() {
		return goHomeThresholdNegSeekBar;
	}

	@Override
	public TextView getGoHomeThresholdPosText() {
		return goHomeThresholdPosText;
	}

	@Override
	public SeekBar getGoHomeThresholdPosSeekBar() {
		return goHomeThresholdPosSeekBar;
	}
}
