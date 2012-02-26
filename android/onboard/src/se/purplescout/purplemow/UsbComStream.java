package se.purplescout.purplemow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class UsbComStream extends ComStream {

	private FileInputStream inputStream;
	private FileOutputStream outputStream;

	public UsbComStream(FileInputStream inputStream,
			FileOutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void sendCommand(byte command, byte target, int value)
			throws IOException {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		Log.w(this.getClass().getSimpleName(),
				"[" + Byte.toString(buffer[0]) + "," + Byte.toString(buffer[1])
						+ "," + Byte.toString(buffer[2]) + "]\n");
		if (outputStream != null && buffer[1] != -1) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e(this.getClass().getName(), e.getMessage(), e);
			}
		}

		
	}

	@Override
	public void read(byte[] buffer) throws IOException {
		inputStream.read(buffer);
	}

}