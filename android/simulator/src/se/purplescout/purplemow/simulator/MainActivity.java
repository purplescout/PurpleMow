package se.purplescout.purplemow.simulator;

import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.core.fsm.event.MotorFSMEvent;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
    private MainFSM mainFSM;
	private MotorFSM motorFSM;
	private SensorReader sensorReader;
	private SimulatorModel model;
	private SimulatorView view;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        view = (SimulatorView) findViewById(R.id.simulatorView);

        model = new SimulatorModel();
        view.setModel(model);

		TextView textView = new TextView(this);
		mainFSM = new MainFSM(textView);
		motorFSM = new MotorFSM(model, textView);
		sensorReader = new SensorReader(model, textView);
		mainFSM.setMotorFSM(motorFSM);
		motorFSM.setMainFSM(mainFSM);
		sensorReader.setMainFSM(mainFSM);

		Log.d(this.getClass().getCanonicalName(), "Starting FSM");
		mainFSM.start();
		motorFSM.start();
		sensorReader.start();

		motorFSM.queueEvent(new MotorFSMEvent(MotorFSMEvent.EventType.MOVE_FWD));
    }

	@Override
	protected void onStop() {
		super.onStop();
		mainFSM.stop();
	}
}