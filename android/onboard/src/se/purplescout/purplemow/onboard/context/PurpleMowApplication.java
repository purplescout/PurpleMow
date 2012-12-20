package se.purplescout.purplemow.onboard.context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import roboguice.RoboGuice;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.backend.service.remote.RemoteService;
import se.purplescout.purplemow.onboard.backend.service.remote.RemoteServiceImpl;
import se.purplescout.purplemow.onboard.backend.service.schedule.ScheduleService;
import se.purplescout.purplemow.onboard.backend.service.schedule.ScheduleServiceImpl;
import se.purplescout.purplemow.onboard.db.sqlhelper.PurpleMowSqliteOpenHelper;
import android.app.Application;

import com.google.inject.AbstractModule;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

public class PurpleMowApplication extends Application {

	ScheduledExecutorService scheduledExecutorService;
	OrmLiteSqliteOpenHelper sqliteOpenHelper;
	ConnectionSource connectionSource;

	@Override
	public void onCreate() {
		super.onCreate();

		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
		connectionSource = sqliteOpenHelper.getConnectionSource();

		RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, RoboGuice.newDefaultRoboModule(this), new AbstractModule() {

			@Override
			protected void configure() {
				bind(ConnectionSource.class).toInstance(connectionSource);
				bind(ScheduleEventDAO.class).to(ScheduleEventDAOImpl.class);
				bind(RemoteService.class).to(RemoteServiceImpl.class);
				bind(ScheduleService.class).to(ScheduleServiceImpl.class);
				bind(ScheduledExecutorService.class).toInstance(scheduledExecutorService);
			}
		});
	}
}
