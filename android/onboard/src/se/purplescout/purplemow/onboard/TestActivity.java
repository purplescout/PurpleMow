package se.purplescout.purplemow.onboard;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import se.purplescout.purplemow.onboard.web.WebServer;
import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
import se.purplescout.purplemow.onboard.web.service.log.LogService;
import se.purplescout.purplemow.onboard.web.service.log.LogServiceImpl;
import se.purplescout.purplemow.onboard.web.service.remote.RemoteService;
import se.purplescout.purplemow.onboard.web.service.remote.RemoteServiceImpl;
import se.purplescout.purplemow.onboard.web.service.schedule.ScheduleService;
import se.purplescout.purplemow.onboard.web.service.schedule.ScheduleServiceImpl;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class TestActivity extends Activity {

	MainFSM mainFSM;
	MotorFSM motorFSM;
	ScheduledExecutorService scheduler;
	SensorReader sensorReader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogCallback logCallback = new LogCallback() {

			@Override
			public void post(LogMessage msg) {
				
			}
		};

		ComStream comStream = new ComStream() {

			Random random = new Random();

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
				buffer[1] = ComStream.BWF_SENSOR_LEFT;
				buffer[2] = 0;

				byte[] randomByte = new byte[1];
				random.nextBytes(randomByte);
				buffer[3] = randomByte[0];
			}
		};
		
		try {
			mainFSM = new MainFSM(logCallback);
			motorFSM = new MotorFSM(comStream, logCallback);
			mainFSM.setMotorFSM(motorFSM);
			motorFSM.setMainFSM(mainFSM);
			scheduler = Executors.newScheduledThreadPool(1);
			sensorReader = new SensorReader(comStream);
			sensorReader.setMainFSM(mainFSM);
			mainFSM.start();
			motorFSM.start();
			sensorReader.start();

			OrmLiteSqliteOpenHelper sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
			ConnectionSource connectionSource = sqliteOpenHelper.getConnectionSource();

			RemoteService remoteService = new RemoteServiceImpl(motorFSM);
			ScheduleEventDAO scheduleEntryDAO = new ScheduleEventDAOImpl(connectionSource);
			ScheduleService scheduleService = new ScheduleServiceImpl(scheduleEntryDAO, scheduler, motorFSM);
			LogService logService = new LogServiceImpl(sensorReader);
			RpcDispatcher dispatcher = new RpcDispatcher(remoteService, scheduleService, logService);
			new WebServer(8080, this, dispatcher);
			scheduleService.initScheduler();
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
