package se.purplescout.purplemow.simulator;

import se.purplescout.purplemow.core.fsm.MainFSM;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private MainFSM mainFSM;
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

        mainFSM = new MainFSM(model.getComStream());
		mainFSM.start();
            
    }

	@Override
	protected void onStop() {
		super.onStop();
		mainFSM.stop();
	}
}