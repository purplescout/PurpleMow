package se.purplescout.purplemow.onboard;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.GuiLogCallback;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.onboard.web.WebServer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity extends Activity {

	MainFSM mainFSM;
	MotorFSM motorFSM;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		GuiLogCallback logCallback = new GuiLogCallback() {

			@Override
			public void post(String msg) {
				
			}
		};

		ComStream comStream = new ComStream() {

			@Override
			public void sendCommand(byte command, byte target, int value) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void sendCommand(byte command, byte target) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void read(byte[] buffer) throws IOException {
				// TODO Auto-generated method stub

			}
		};
		
		try {
			mainFSM = new MainFSM(logCallback);
			motorFSM = new MotorFSM(comStream, logCallback);
			motorFSM.setMainFSM(mainFSM);
			mainFSM.start();
			motorFSM.start();
			new WebServer(8080, this, new RemoteController(motorFSM));
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
		}
	}

	@Override
	protected void onDestroy() {
		
	}
}
