package se.purplescout.purplemow.onboard.ui.splash.activity;

import se.purplescout.purplemow.onboard.R;
import se.purplescout.purplemow.onboard.ui.controller.PlaceController;
import se.purplescout.purplemow.onboard.ui.home.place.HomePlace;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		AsyncTask<Void, Void, Void> asTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				PlaceController.goTo(SplashActivity.this, new HomePlace());
			}
		};
		asTask.execute();
	}
}
