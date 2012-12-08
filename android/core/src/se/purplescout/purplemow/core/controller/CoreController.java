package se.purplescout.purplemow.core.controller;

import se.purplescout.purplemow.core.ComStream;

public interface CoreController extends SensorLogger {

	void prepare(ComStream comStream);

	void start();

	void shutdown();
}
