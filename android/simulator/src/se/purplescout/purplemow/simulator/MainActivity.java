package se.purplescout.purplemow.simulator;

import se.purplescout.purplemow.MainFsm;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private MainFsm mainFsm;
	private SimulatorModel model;
	private SimulatorView view;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        view = (SimulatorView) findViewById(R.id.simulatorView);


        if (savedInstanceState == null) {
            model = new SimulatorModel();
            view.setModel(model);

            mainFsm = new MainFsm(model.getComStream());
    		mainFsm.start();
        } else {
        	// TODO
        }        
    }
}