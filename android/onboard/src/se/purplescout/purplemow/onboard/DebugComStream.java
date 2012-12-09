package se.purplescout.purplemow.onboard;

import java.io.IOException;

import se.purplescout.purplemow.core.ComStream;
import android.util.Log;

public class DebugComStream extends ComStream {

	private static final byte CMD_SEND = 0x1;
	private static final byte CMD_WRITE = 0x2;
	private static final byte CMD_RELAY = 0x3;
	private static final byte CMD_READ = 0x4;

	@Override
	public void sendCommand(byte command, byte target, int value) throws IOException {
		Log.i(getClass().getSimpleName(), String.format("Received command %s %s %s", command, target, value));
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
}
