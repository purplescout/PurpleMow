package se.purpleout.purplemow;

import java.io.IOException;

public class SensorReader {
	
	private ComStream comStream;
	
	public SensorReader(UsbComStream usbComStream) {
		this.comStream = usbComStream;
	}


	/**
	 * Range sensor. Values 0 - 1023. 
	 * @return
	 * @throws IOException
	 */
	public int readDistance() throws IOException{
		comStream.readSensor();
		
		byte[] buffer = new byte[4];
		comStream.read(buffer);
		//TODO: How to interpret value from distance sensor? Combine multiple bytes?
		return buffer[3];
	}
	
	/**
	 * BWF sensor. Values 0 - ? 
	 * @return
	 */
	public int readBWF() {
		//TODO: Implement!
		return 0;
	}
}
