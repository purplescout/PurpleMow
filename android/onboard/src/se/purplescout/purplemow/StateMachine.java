package se.purplescout.purplemow;

import android.util.Log;

/**
 * This class is the Main program. It is responsible for handling states and react accordingly, a so called State
 * Machine!
 * 
 */
public class StateMachine implements Runnable {

	private static final int TOO_FUCKING_CLOSE = 100;
	private MotorController mc;
	private SensorReader sr;

	public StateMachine(MotorController mc, SensorReader sr) {
		this.mc = mc;
		this.sr = sr;
	}

	@Override
	public void run() {
		try {
			MotorState ms = new MotorState();
			ms.setMotorLeft(0);
			ms.setMotorRight(0);
			// set motors to stop
			mc.stop(ms);

			// boot sequence...
			// what here?

			// check all sensors for current status
			// this far only one sensor to read...
			int dist = 0;
			dist = sr.readDistance();

			mc.moveForward(ms);

			// Loop forever!
			while (true) {

				// This is the first state of the state machine
				if (ms.getMotorLeft() == 1 && ms.getMotorRight() == 1 && dist < TOO_FUCKING_CLOSE) {
					// do some magic to avoid obstacle
					mc.avoidObstacleLeftSide(ms);
				}

				// TODO: Implement all states. Possibly something smarter than a bunch of if-statements...
				// Any more states?

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(this.getClass().getName(), e.getMessage());
		}

	}

}
