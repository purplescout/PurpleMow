package se.purplescout.purplemow.core.controller;

import se.purplescout.purplemow.core.ComStream;
import se.purplescout.purplemow.core.common.Constants;

public interface CoreController extends SensorLogger {

	void prepare(ComStream comStream, Constants constants);

	void start();

	void shutdown();
}