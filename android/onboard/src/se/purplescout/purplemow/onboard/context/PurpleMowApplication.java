package se.purplescout.purplemow.onboard.context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import roboguice.RoboGuice;
import se.purplescout.purplemow.core.controller.CoreController;
import se.purplescout.purplemow.core.controller.CoreControllerImpl;
import se.purplescout.purplemow.core.controller.SensorLogger;
import se.purplescout.purplemow.onboard.backend.dao.constant.ConstantDAO;
import se.purplescout.purplemow.onboard.backend.dao.constant.ConstantDAOImpl;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAO;
import se.purplescout.purplemow.onboard.backend.dao.schedule.ScheduleEventDAOImpl;
import se.purplescout.purplemow.onboard.backend.service.constant.ConstantService;
import se.purplescout.purplemow.onboard.backend.service.constant.ConstantServiceImpl;
import se.purplescout.purplemow.onboard.backend.service.log.LogService;
import se.purplescout.purplemow.onboard.backend.service.log.LogServiceImpl;
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
	CoreController coreController;

	@Override
	public void onCreate() {
		super.onCreate();

		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		sqliteOpenHelper = new PurpleMowSqliteOpenHelper(this);
		connectionSource = sqliteOpenHelper.getConnectionSource();
		coreController = new CoreControllerImpl();

		RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, RoboGuice.newDefaultRoboModule(this), new AbstractModule() {

			@Override
			protected void configure() {
				bind(ConnectionSource.class).toInstance(connectionSource);
				bind(ScheduleEventDAO.class).to(ScheduleEventDAOImpl.class);
				bind(ConstantDAO.class).to(ConstantDAOImpl.class);
				bind(RemoteService.class).to(RemoteServiceImpl.class);
				bind(ScheduleService.class).to(ScheduleServiceImpl.class);
				bind(ScheduledExecutorService.class).toInstance(scheduledExecutorService);
				bind(LogService.class).to(LogServiceImpl.class);
				bind(ConstantService.class).to(ConstantServiceImpl.class);
				bind(SensorLogger.class).toInstance(coreController);
				bind(CoreController.class).toInstance(coreController);
			}
		});
	}
}
