package se.purplescout.purplemow.onboard.backend.service.log;

import java.io.InputStream;
import java.util.List;

import se.purplescout.purplemow.onboard.shared.log.dto.LogcatFilterDTO;

public interface LogService {

	InputStream getLogcat();

	InputStream getLogcatAsJSON(List<LogcatFilterDTO> filterDTOs);
}
