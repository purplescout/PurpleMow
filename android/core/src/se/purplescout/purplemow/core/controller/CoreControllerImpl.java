package se.purplescout.purplemow.core.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.common.Constants;
import se.purplescout.purplemow.core.fsm.motor.MotorFSM;
import se.purplescout.purplemow.core.fsm.mower.MainFSM;
import android.util.Log;

public class CoreControllerImpl implements CoreController {

	private enum State {
		PREPARED, STARTED, SHUTDOWN
	}

	private State state = State.SHUTDOWN;
	private MainFSM mowerFSM;
	private MotorFSM motorFSM;
	private SensorReader sensorReader;
	private ComStream comStream;

	@Override
	public void prepare(ComStream comStream, Constants constants) {
		if (state == State.STARTED) {
			throw new IllegalStateException("Prepare cant be called after start. Call shutdown before trying to call prepare");
		}
		mowerFSM = new MainFSM(constants);
		motorFSM = new MotorFSM(constants, comStream);
		sensorReader = new SensorReader(comStream);
		this.comStream = comStream;
		state = State.PREPARED;
	}

	@Override
	public void start() {
		if (state != State.PREPARED) {
			throw new IllegalStateException("Prepare must be called before trying to call start");
		}
		mowerFSM.start();
		motorFSM.start();
		sensorReader.start();
		state = State.STARTED;
	}

	@Override
	public void shutdown() {
		if (state != State.SHUTDOWN) {
			mowerFSM.shutdown();
			motorFSM.shutdown();
			sensorReader.shutdown();
			try {
				comStream.close();
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
			state = State.SHUTDOWN;
		}
	}

	@Override
	public List<SensorData> getBwfSensorData() {
		if (state == State.SHUTDOWN) {
			return new ArrayList<SensorData>();
		}
		return sensorReader.getBwfSensorData();
	}

	//	@Override
//	public List<SensorData> getLeftRangeSensorData() {
//		if (state == State.SHUTDOWN) {
//			return new ArrayList<SensorData>();
//		}
//		return sensorReader.getLeftRangeSensorData();
//	}
//
//	@Override
//	public List<SensorData> getRightRangeSensorData() {
//		if (state == State.SHUTDOWN) {
//			return new ArrayList<SensorData>();
//		}
//		return sensorReader.getRightRangeSensorData();
//	}
}
