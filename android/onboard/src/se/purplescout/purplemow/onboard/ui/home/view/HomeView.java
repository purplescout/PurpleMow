package se.purplescout.purplemow.onboard.ui.home.view;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.common.binder.ActivityBinderView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.ContentView;
import se.purplescout.purplemow.onboard.ui.common.binder.annotation.UiField;
import se.purplescout.purplemow.onboard.ui.home.activity.HomeActivity;
import android.app.ProgressDialog;
import android.widget.Button;

@ContentView(R.layout.activity_home)
public class HomeView extends ActivityBinderView implements HomeActivity.ViewDisplay {

	@UiField(R.id.homeRemoteBtn) Button homeRemoteBtn;
	@UiField(R.id.homeScheduleBtn) Button homeScheduleBtn;
	@UiField(R.id.homeCalibrateBtn) Button homeCalibrateBtn;
	@UiField(R.id.homeSensorsBtn) Button homeSensorsBtn;
	@UiField(R.id.homeLogsBtn) Button homeLogsBtn;
	@UiField(R.id.homeSettingsBtn) Button homeSettingsBtn;
	@UiField(R.id.homeStartBtn) Button homeStartBtn;
	private final ProgressDialog loadingDialog;
	
	public HomeView(HomeActivity activity) {
		super(activity);
		loadingDialog = new ProgressDialog(activity);
	}

	@Override
	public Button getRemoteBtn() {
		return homeRemoteBtn;
	}

	@Override
	public Button getScheduleBtn() {
		return homeScheduleBtn;
	}

	@Override
	public Button getCalibrateBtn() {
		return homeCalibrateBtn;
	}

	@Override
	public Button getSensorsBtn() {
		return homeSensorsBtn;
	}

	@Override
	public Button getLogsBtn() {
		return homeLogsBtn;
	}

	@Override
	public Button getSettingsBtn() {
		return homeSettingsBtn;
	}

	@Override
	public Button getStartBtn() {
		return homeStartBtn;
	}

	@Override
	public void setLoading(boolean show) {
		if (show) {
			loadingDialog.show();
		} else {
			loadingDialog.hide();
		}
	}
}