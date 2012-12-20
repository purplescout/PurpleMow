package se.purplescout.purplemow.onboard.db.migration;

import java.sql.SQLException;

import se.purplescout.purplemow.onboard.db.entity.Constant;
import se.purplescout.purplemow.onboard.shared.constant.enums.ConstantEnum;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class MigrationScript  {

	public static void execute(SQLiteDatabase db, ConnectionSource conn, int databaseVersion) throws SQLException {		
		if (databaseVersion < 5) {
			db.execSQL("delete from schedule_event");
			instertV5(db);
		}
		if (databaseVersion < 6) {
			insertV6(db, conn);
		}
	}

	public static void insertData(SQLiteDatabase db, ConnectionSource conn) throws SQLException {
		instertV5(db);
		insertV6(db, conn);
	}

	private static void insertV6(SQLiteDatabase db, ConnectionSource conn) throws SQLException {
		TableUtils.createTable(conn, Constant.class);
		db.execSQL("insert into constant (id, name, value) values(1, '" + ConstantEnum.FULL_SPEED + "', 245)");
		db.execSQL("insert into constant (id, name, value) values(2, '" + ConstantEnum.NO_SPEED + "', 0)");
		db.execSQL("insert into constant (id, name, value) values(3, '" + ConstantEnum.RANGE_LIMIT + "', 290)");
		db.execSQL("insert into constant (id, name, value) values(4, '" + ConstantEnum.BWF_LIMIT + "', 870)");
		db.execSQL("insert into constant (id, name, value) values(5, '" + ConstantEnum.BATTERY_LOW + "', 100)");
		db.execSQL("insert into constant (id, name, value) values(6, '" + ConstantEnum.BATTERY_CHARGED + "', 900)");
		db.execSQL("insert into constant (id, name, value) values(7, '" + ConstantEnum.GO_HOME_HYSTERES + "', 5)");
		db.execSQL("insert into constant (id, name, value) values(8, '" + ConstantEnum.GO_HOME_THRESHOLD_NEG + "', 15)");
		db.execSQL("insert into constant (id, name, value) values(9, '" + ConstantEnum.GO_HOME_THRESHOLD_POS + "', 30)");
		db.execSQL("insert into constant (id, name, value) values(10, '" + ConstantEnum.GO_HOME_OFFSET + "', 380)");
	}
	
	private static void instertV5(SQLiteDatabase db) {
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-22 12:30:00.000', '2012-07-22 10:00:00.000', 1, 1)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-23 12:30:00.000', '2012-07-23 10:00:00.000', 1, 2)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-24 12:30:00.000', '2012-07-24 10:00:00.000', 1, 3)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-25 12:30:00.000', '2012-07-25 10:00:00.000', 1, 4)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-26 12:30:00.000', '2012-07-26 10:00:00.000', 1, 5)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-27 12:30:00.000', '2012-07-27 10:00:00.000', 1, 6)");
		db.execSQL("insert into schedule_event (type, interval, stop_date, start_date, is_active, id) values('MOWING', 'WEEKLY','2012-07-28 12:30:00.000', '2012-07-28 10:00:00.000', 1, 7)");
	}
}
