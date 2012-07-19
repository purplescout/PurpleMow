package se.purplescout.purplemow.onboard.db.migration;

import com.j256.ormlite.support.ConnectionSource;

import android.database.sqlite.SQLiteDatabase;

public class MigrationScript  {

	public static void execute(SQLiteDatabase db, ConnectionSource conn, int databaseVersion) {
		if (databaseVersion < 2) {
			//testing
			db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-22 12:30:00.000', '2012-07-22 10:00:00.000', 1, 1)");
		}
	}
}
