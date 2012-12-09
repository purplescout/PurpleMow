package se.purplescout.purplemow.onboard.ui.remote.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.ActivityBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.remote.activity.RemoteActivity;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

@ContentView(R.layout.activity_remote)
public class RemoteView extends ActivityBinderView implements RemoteActivity.ViewDisplay {

	@UiField(R.id.forwardBtn) Button forwardBtn;
	@UiField(R.id.leftBtn) Button leftBtn;
	@UiField(R.id.rightBtn) Button rightBtn;
	@UiField(R.id.reverseBtn) Button reverseBtn;
	@UiField(R.id.stopBtn) Button stopBtn;
	@UiField(R.id.incrementCutterBtn) Button incrementCutterBtn;
	@UiField(R.id.decrementCutterBtn) Button decrementCutterBtn;
	
	public RemoteView(Activity activity) {
		super(activity);
	}

	@Override
	public Button getForwardBtn() {
		return forwardBtn;
	}

	@Override
	public Button getLeftBtn() {
		return leftBtn;
	}

	@Override
	public Button getRightBtn() {
		return rightBtn;
	}

	@Override
	public Button getReverseBtn() {
		return reverseBtn;
	}

	@Override
	public Button getIncrementCutterBtn() {
		return incrementCutterBtn;
	}

	@Override
	public Button getDecrementCutterBtn() {
		return decrementCutterBtn;
	}

	@Override
	public View getStopBtn() {
		return stopBtn;
	}
}
