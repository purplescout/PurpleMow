package se.purplescout.purplemow.onboard.backend.core;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import android.util.Log;

public class DebugComStream extends ComStream {

	@Override
	public void sendCommand(byte command, byte target, int value) throws IOException {
		Log.d(getClass().getSimpleName(), String.format("Received command %s %s %s", command, target, value));
	}

	@Override
	public void sendCommand(byte command, byte target) throws IOException {
		Log.d(getClass().getSimpleName(), String.format("Received command %s %s", command, target));
	}

	@Override
	public void read(byte[] buffer) throws IOException {
		buffer[1] = 0x3;
		buffer[2] = 0x3;
		buffer[3] = (byte) 0xff;
	}

	@Override
	public void close() throws IOException {
		Log.d(getClass().getSimpleName(), String.format("Closing stream"));
	}
}
