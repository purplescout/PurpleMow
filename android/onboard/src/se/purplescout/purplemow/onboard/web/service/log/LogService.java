package se.purplescout.purplemow.onboard.web.service.log;

import java.io.InputStream;

public interface LogService {

	InputStream getLogcatAsHTML();

	InputStream getLeftBwfData();

	InputStream getLogcat();

	InputStream getRightBwfData();

	InputStream getLeftRangeData();

	InputStream getRightRangeData();
}
