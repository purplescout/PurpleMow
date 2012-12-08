package se.purplescout.purplemow.core.controller;

import java.util.Date;
import java.util.List;

public interface SensorLogger {

	public class SensorData {

		private Date date;
		private int value;

		public SensorData(Date date, int value) {
			this.date = date;
			this.value = value;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}
	}

	List<SensorData> getBwfSensorData();

	List<SensorData> getLeftRangeSensorData();

	List<SensorData> getRightRangeSensorData();
}
