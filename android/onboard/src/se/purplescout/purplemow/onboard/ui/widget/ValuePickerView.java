package se.purplescout.purplemow.onboard.ui.widget;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.SimpleOnSeekBarChangeListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ValuePickerView extends LinearLayout {
	
	public interface ValuePickListener {
		
		void onValuePicked(int value);
	}
	
	private int value;
	private String title;
	private int maxValue;
	private int minValue;
	
	private TextView titleText;
	private TextView valueText;
	private SeekBar seekBar;
	private Button incrementButton;
	private Button decrementButton;
	
	private ValuePickListener valueChangeListener;
	
	public ValuePickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ValuePickerView);
		title = typedArray.getString(R.styleable.ValuePickerView_title);
		minValue = 0;
		maxValue = typedArray.getInteger(R.styleable.ValuePickerView_maxValue, 10);
		typedArray.recycle();
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setValue(int value) {
		this.value = value;
		seekBar.setSecondaryProgress(value);
		updateView();
	}

	public void setOnValueChangeListener(ValuePickListener valueChangeListener) {
		this.valueChangeListener = valueChangeListener;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_valuepicker, this);
		setupViewItems();
		bind();
	}
	
	private void setupViewItems() {
		titleText = (TextView) findViewById(R.id.valuePickerTitleText);
		valueText = (TextView) findViewById(R.id.valuePickerValueText);
		seekBar = (SeekBar) findViewById(R.id.valuePickerSeekBar);
		incrementButton = (Button) findViewById(R.id.valuePickerIncBtn);
		decrementButton = (Button) findViewById(R.id.valuePickerDecBtn);
	}

	private void bind() {
		seekBar.setMax(maxValue);
		incrementButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				value++;
				if (value > maxValue) {
					value = maxValue;
				}
				invokeListener();
				updateView();
			}
		});
		decrementButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				value--;
				if (value < minValue) {
					value = minValue;
				}
				invokeListener();
				updateView();
			}
		});
		seekBar.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				value = progress;
				invokeListener();
				updateView();
			}
		});
	}

	private void updateView() {
		titleText.setText(title);
		valueText.setText(String.format("%d", value));
		seekBar.setProgress(value);
	}

	private void invokeListener() {
		if (valueChangeListener != null) {
			valueChangeListener.onValuePicked(value);
		}
	}
}