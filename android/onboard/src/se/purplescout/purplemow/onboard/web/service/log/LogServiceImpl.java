package se.purplescout.purplemow.onboard.web.service.log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import se.purplescout.purplemow.core.SensorReader;
import se.purplescout.purplemow.core.SensorReader.SensorData;
import android.util.Log;

public class LogServiceImpl implements LogService {

	private static DateFormat dateFormat = new SimpleDateFormat("MM-dd hh:mm:ss.SSS");

	private SensorReader sensorReader;

	public LogServiceImpl(SensorReader sensorReader) {
		this.sensorReader = sensorReader;
	}

	@Override
	public InputStream getLogcatAsHTML() {
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String color = "";
				if (line.charAt(0) == 'I') {
					color = "green";
				} else if (line.charAt(0) == 'D') {
					color = "blue";
				} else {
					color = "black";
				}
				log.append(String.format("<span style=\"color: %s\">", color));
				log.append(StringEscapeUtils.escapeHtml(line));
				log.append("</span><br/>");
			}

			return new ByteArrayInputStream(log.toString().getBytes());
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getLogcat() {
		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line).append("\n");
			}
			log.replace(log.length() - 1, log.length(), "");

			return new ByteArrayInputStream(log.toString().getBytes());
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getLeftBwfData() {
		List<SensorData> sendorData = sensorReader.getBwfLeft();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getRightBwfData() {
		List<SensorData> sendorData = sensorReader.getBwfRight();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getLeftRangeData() {
		List<SensorData> sendorData = sensorReader.getRangeLeft();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getRightRangeData() {
		List<SensorData> sendorData = sensorReader.getRangeRight();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	private String createSensorOutput(List<SensorData> sendorData) {
		StringBuilder log = new StringBuilder();
		for (SensorData data : sendorData) {
			log.append(dateFormat.format(data.getDate())).append(",").append(data.getValue()).append("\n");
		}

		return log.toString();
	}
}
