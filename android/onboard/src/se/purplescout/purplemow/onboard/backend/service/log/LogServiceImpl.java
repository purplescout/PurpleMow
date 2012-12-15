package se.purplescout.purplemow.onboard.backend.service.log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.inject.Inject;

import se.purplescout.purplemow.core.controller.SensorLogger;
import se.purplescout.purplemow.core.controller.SensorLogger.SensorData;
import se.purplescout.purplemow.onboard.shared.log.dto.LogcatFilterDTO;
import android.util.Log;

public class LogServiceImpl implements LogService {

	private static final String LOGCAT_CMD = "logcat -d -v time";
	private static final DateFormat dateFormat = new SimpleDateFormat("MM-dd hh:mm:ss.SSS");

	private SensorLogger sensorLogger;

	@Inject
	public LogServiceImpl(SensorLogger sensorReader) {
		this.sensorLogger = sensorReader;
	}

	@Override
	public InputStream getLogcatAsJSON(List<LogcatFilterDTO> filterDTOs) {
		StringBuilder filter = new StringBuilder();
		for (LogcatFilterDTO filterDTO : filterDTOs) {
			filter.append(filterDTO.getTag()).append(":").append(filterDTO.getPriorityConstant()).append(" ");
		}
		// Remove trailing ' '
		if (filter.length() > 0) {
			filter.replace(filter.length() - 1, filter.length(), "");
		}
		
		try {
			Process process = Runtime.getRuntime().exec(String.format("%s %s *:S", LOGCAT_CMD, filter.toString()));
			InputStream inputStream = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			StringBuilder json = new StringBuilder();
			String line;
			json.append("[");
			while ((line = bufferedReader.readLine()) != null) {
				String[] logEntry = line.split(" ");
				if (logEntry.length < 4) {
					continue;
				}
				json.append("{");
				char priorityConstant = logEntry[2].charAt(0);
				json.append("\"priorityConstant\"").append(":").append("\"" + priorityConstant + "\"");
				json.append(",");
				json.append("\"entry\"").append(":").append("\"" + StringEscapeUtils.escapeJavaScript(line) + "\"");
				json.append("}");
				json.append(",");
			}
			// Remove trailing ','
			if (json.length() > 1) {
				json.replace(json.length() - 1, json.length(), "");
			}
			json.append("]");

			return new ByteArrayInputStream(json.toString().getBytes());
		} catch (IOException e) {
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getLogcat() {
		try {
			Process process = Runtime.getRuntime().exec(LOGCAT_CMD);
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
			Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public InputStream getLeftBwfData() {
		List<SensorData> sendorData = sensorLogger.getBwfSensorData();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getRightBwfData() {
		List<SensorData> sendorData = sensorLogger.getBwfSensorData();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getLeftRangeData() {
		List<SensorData> sendorData = sensorLogger.getLeftRangeSensorData();
		String log = createSensorOutput(sendorData);

		return new ByteArrayInputStream(log.getBytes());
	}

	@Override
	public InputStream getRightRangeData() {
		List<SensorData> sendorData = sensorLogger.getRightRangeSensorData();
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
