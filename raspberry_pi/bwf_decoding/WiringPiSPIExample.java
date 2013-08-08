
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  WiringPiSPIExample.java  
 * 
 * This file is part of the Pi4J project. More information about 
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2013 Pi4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.wiringpi.Spi;

public class WiringPiSPIExample {

	@SuppressWarnings("unused")
	public static void main(String args[]) throws InterruptedException {

		//
		// This SPI example is using the WiringPi native library to communicate
		// with
		// the SPI hardware interface connected to a MCP23S17 I/O Expander.
		//
		// Please note the following command are required to enable the SPI
		// driver on
		// your Raspberry Pi:
		// > sudo modprobe spi_bcm2708
		// > sudo chown `id -u`.`id -g` /dev/spidev0.*
		//
		// this source code was adapted from:
		// https://github.com/thomasmacpherson/piface/blob/master/python/piface/pfio.py
		//
		// see this blog post for additional details on SPI and WiringPi
		// https://projects.drogon.net/understanding-spi-on-the-raspberry-pi/
		//
		// see the link below for the data sheet on the MCP23S17 chip:
		// http://ww1.microchip.com/downloads/en/devicedoc/21952b.pdf

		System.out.println("<--Pi4J--> SPI test program using MCP23S17 I/O Expander Chip");

		// setup SPI for communication
		int fd = Spi.wiringPiSPISetup(0, 1000000);
		
		if (fd <= -1) {
			System.out.println(" ==>> SPI SETUP FAILED");
			return;
		}

		while (true) {
			byte packet[] = new byte[3];
			packet[0] =6; // address byte
			packet[1] = (byte) 0; // register byte
			packet[2] = 0; // data byte
			System.out.println(String.format("Skickar: Paket0: %x Paket1: %x Paket2: %x", packet[0], packet[1], packet[2]));
			Spi.wiringPiSPIDataRW(0, packet, 3);
			System.out.println(String.format("Ignore: %x", packet[0]));
			System.out.println(String.format("MSB: %x LSB: %x", packet[1], packet[2]));
			System.out.println("jadå: "+bytesToHex(packet));
			//int adcout = ((packet[1]&0xFF) << 8) + packet[2];
			int adcVal = composeInt(packet[1], packet[2]);
			System.out.println(adcVal);
			Thread.sleep(1000);
		}
	}
   public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }   
        private static int composeInt(byte hi, byte lo) {
                int val = hi & 0x0f;
                val *= 256;
                val += lo & 0xff;
                return val;
        }

}
