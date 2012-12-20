package se.purplescout.purplemow.onboard.backend.service.log;

import java.io.InputStream;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatFilterDTO;

public interface LogService {

	InputStream getLeftBwfData();

	InputStream getLogcat();

	InputStream getRightBwfData();

	InputStream getLeftRangeData();

	InputStream getRightRangeData();

	InputStream getLogcatAsJSON(List<LogcatFilterDTO> filterDTOs);
}
