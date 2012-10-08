package se.purplescout.purplemow.onboard;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.LogCallback;
import se.purplescout.purplemow.core.LogMessage;
import se.purplescout.purplemow.core.fsm.MainFSM;
import se.purplescout.purplemow.core.fsm.MotorFSM;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import se.purplescout.purplemow.onboard.web.WebServer;
import se.purplescout.purplemow.onboard.web.dispatcher.RpcDispatcher;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogCallback logCallback = new LogCallback() {

			@Override
			public void post(LogMessage msg) {
				
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
			scheduler = Executors.newScheduledThreadPool(1);
			mainFSM.start();
			motorFSM.start();

			OrmLiteSqliteOpenHelper sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
			ConnectionSource connectionSource = sqliteOpenHelper.getConnectionSource();

			RemoteService remoteService = new RemoteServiceImpl(motorFSM);
			ScheduleEventDAO scheduleEntryDAO = new ScheduleEventDAOImpl(connectionSource);
			ScheduleService scheduleService = new ScheduleServiceImpl(scheduleEntryDAO, scheduler, motorFSM);
			RpcDispatcher dispatcher = new RpcDispatcher(remoteService, scheduleService);
			new WebServer(8080, this, dispatcher);
			scheduleService.initScheduler();
		} catch (IOException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			Log.e(this.getClass().getCanonicalName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
