package se.purplescout.purplemow.onboard.ui.configure.activity;

import roboguice.activity.RoboActivity;
import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.backend.service.constant.ConstantService;
import se.purplescout.purplemow.onboard.shared.constant.dto.ConstantsDTO;
import se.purplescout.purplemow.onboard.ui.common.SimpleOnSeekBarChangeListener;
import se.purplescout.purplemow.onboard.ui.common.SimpleTextWatcher;
import se.purplescout.purplemow.onboard.ui.configure.view.ConfigureView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.inject.Inject;

public class ConfigureActivity extends RoboActivity {

	public interface ViewDisplay {

		EditText getFullSpeedText();

		EditText getNoSpeedText();

		TextView getRangeLimitText();

		SeekBar getRangeLimitSeekBar();

		TextView getBwfLimitText();

		SeekBar getBwfLimitSeekBar();

		TextView getBatteryLowText();

		SeekBar getBatteryLowSeekBar();

		TextView getBatteryChargedText();

		SeekBar getBatteryChargedSeekBar();

		TextView getGoHomeOffsetText();

		SeekBar getGoHomeOffsetSeekBar();

		TextView getGoHomeHysteresText();

		SeekBar getGoHomeHysteresSeekBar();

		TextView getGoHomeThresholdPosText();

		SeekBar getGoHomeThresholdPosSeekBar();

		TextView getGoHomeThresholdNegText();

		SeekBar getGoHomeThresholdNegSeekBar();
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
		display.getRangeLimitSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setRangeLimit(progress);
				constants.setChanged(true);
				display.getRangeLimitText().setText(String.format("%d", progress));
			}
		});
		display.getBwfLimitSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setBwfLimit(progress);
				constants.setChanged(true);
				display.getBwfLimitText().setText(String.format("%d", progress));
			}
		});
		display.getBatteryLowSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setBatteryLow(progress);
				constants.setChanged(true);
				display.getBatteryLowText().setText(String.format("%d", progress));
			}
		});
		display.getBatteryChargedSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setBatteryCharged(progress);
				constants.setChanged(true);
				display.getBatteryChargedText().setText(String.format("%d", progress));
			}
		});
		display.getGoHomeOffsetSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setGoHomeOffset(progress);
				constants.setChanged(true);
				display.getGoHomeOffsetText().setText(String.format("%d", progress));
			}
		});
		display.getGoHomeHysteresSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setGoHomeHysteres(progress);
				constants.setChanged(true);
				display.getGoHomeHysteresText().setText(String.format("%d", progress));
			}
		});
		display.getGoHomeThresholdPosSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setGoHomeThresholdPos(progress);
				constants.setChanged(true);
				display.getGoHomeThresholdPosText().setText(String.format("%d", progress));
			}
		});
		display.getGoHomeThresholdNegSeekBar().setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				constants.setGoHomeThresholdNeg(progress);
				constants.setChanged(true);
				display.getGoHomeThresholdNegText().setText(String.format("%d", progress));
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

		display.getRangeLimitText().setText(String.format("%d", constants.getRangeLimit()));
		display.getRangeLimitSeekBar().setProgress(constants.getRangeLimit());
		display.getRangeLimitSeekBar().setSecondaryProgress(constants.getRangeLimit());

		display.getBwfLimitText().setText(String.format("%d", constants.getBwfLimit()));
		display.getBwfLimitSeekBar().setProgress(constants.getBwfLimit());
		display.getBwfLimitSeekBar().setSecondaryProgress(constants.getBwfLimit());

		display.getBatteryLowText().setText(String.format("%d", constants.getBatteryLow()));
		display.getBatteryLowSeekBar().setProgress(constants.getBatteryLow());
		display.getBatteryLowSeekBar().setSecondaryProgress(constants.getBatteryLow());

		display.getBatteryChargedText().setText(String.format("%d", constants.getBatteryCharged()));
		display.getBatteryChargedSeekBar().setProgress(constants.getBatteryCharged());
		display.getBatteryChargedSeekBar().setSecondaryProgress(constants.getBatteryCharged());
		
		display.getGoHomeHysteresText().setText(String.format("%d", constants.getGoHomeHysteres()));
		display.getGoHomeHysteresSeekBar().setProgress(constants.getGoHomeHysteres());
		display.getGoHomeHysteresSeekBar().setSecondaryProgress(constants.getGoHomeHysteres());

		display.getGoHomeOffsetText().setText(String.format("%d", constants.getGoHomeOffset()));
		display.getGoHomeOffsetSeekBar().setProgress(constants.getGoHomeOffset());
		display.getGoHomeOffsetSeekBar().setSecondaryProgress(constants.getGoHomeOffset());
		
		display.getGoHomeThresholdPosText().setText(String.format("%d", constants.getGoHomeThresholdPos()));
		display.getGoHomeThresholdPosSeekBar().setProgress(constants.getGoHomeThresholdPos());
		display.getGoHomeThresholdPosSeekBar().setSecondaryProgress(constants.getGoHomeThresholdPos());

		display.getGoHomeThresholdNegText().setText(String.format("%d", constants.getGoHomeThresholdNeg()));
		display.getGoHomeThresholdNegSeekBar().setProgress(constants.getGoHomeThresholdNeg());
		display.getGoHomeThresholdNegSeekBar().setSecondaryProgress(constants.getGoHomeThresholdNeg());
	}
}