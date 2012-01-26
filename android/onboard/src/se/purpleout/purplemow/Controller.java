package se.purpleout.purplemow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Controller extends Thread {
	IRobot mRobot;
	TextView textView;

	public Controller(IRobot robot, TextView textView) {
		mRobot = robot;
		this.textView = textView;
	}

	@Override
	public void run() {
		String TAG = this.getClass().getSimpleName();
		while (true) {
			try {
				if (mRobot.isConnected()) {
					mRobot.readSensor();
					Thread.sleep(50);
//					Log.w(TAG, "Moving forward");
//					mRobot.moveForward();
//					Log.w(TAG, "Started");
//					Thread.sleep(10000);
//					
//					Log.w(TAG, "Stoping");
//					mRobot.stop();
//					Log.w(TAG, "Stopped");
//					
//					Thread.sleep(5000);
					// mRobot.turnLeft();
					// Thread.sleep(500);
					// mRobot.stop();
					//
					// mRobot.turnRight();
					// Thread.sleep(500);
					// mRobot.stop();
					//
					//
//					 mRobot.moveBackward();
//					 Thread.sleep(5000);
//					 mRobot.stop();
//					 Thread.sleep(5000);
					//
					// mRobot.turnLeft();
					// Thread.sleep(500);
					// mRobot.stop();
					//
					// mRobot.turnRight();
					// Thread.sleep(500);
					// mRobot.stop();
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			} 
		}
	}

}
