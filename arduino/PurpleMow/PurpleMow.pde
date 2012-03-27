

#include <Wire.h>
#include <Servo.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define  MOTOR_RIGHT    8
#define  MOTOR_LEFT     9
#define  CUTTER      	11

#define  RELAY_RIGHT         A0
#define  RELAY_LEFT         A1

#define  RANGE_SENSOR   A2
#define  MOIST_SENSOR    A3
#define  VOLTAGE_SENSOR    A4

#define  BWF_SENSOR_RIGHT    A5
#define  BWF_SENSOR_LEFT    A6

#define I2C_SDA	20
#define I2C_SCL	21

#define ONBOARD_LED 13

AndroidAccessory acc("PurpleScout AB",
		     "PurpleMow",
		     "PurpleMow Arduino Board",
		     "1.0",
		     "http://www.purplescout.se/blog/category/purplemow",
		     "0000000012345678");

void setup() {
	Serial.begin(115200);
	Serial.print("\r\nStart");
 	pinMode(MOTOR_RIGHT, OUTPUT);   // sets the pin as output
	pinMode(MOTOR_LEFT, OUTPUT);   // sets the pin as output
	pinMode(CUTTER, OUTPUT);   // sets the pin as output
  	pinMode(RELAY_RIGHT, OUTPUT);
	pinMode(RELAY_LEFT, OUTPUT);
	pinMode(RANGE_SENSOR, INPUT);
	pinMode(MOIST_SENSOR, INPUT);
	pinMode(VOLTAGE_SENSOR, INPUT);
	pinMode(BWF_SENSOR_RIGHT, INPUT);
	pinMode(BWF_SENSOR_LEFT, INPUT);
        pinMode(ONBOARD_LED, OUTPUT);
	acc.powerOn();
}
// Byte 1: 2 för att prata med motorer, 3 för att prata med relän
// Byte 2: 0 för höger, 1 för vänster, 2 för klippmotor

void switch_led()
{
	static int led_on = HIGH;

	led_on = led_on == LOW ? HIGH : LOW;

	digitalWrite(ONBOARD_LED, led_on);
}

void loop() {
	byte err;
	byte idle;
	static byte count = 0;
	byte msg[4];
	long touchcount;

	if (acc.isConnected()) {
		int len = acc.read(msg, 3, 1);
		int i;
		byte b;
		int16_t val;
		int x, y;
		char c0;
		if (len > 0) {
			// assumes only one command per packet
			if (msg[0] == 0x2) {
				if (msg[1] == 0x0)
				{
					analogWrite(MOTOR_RIGHT, 255 - msg[2]);
					switch_led();
				}
				else if (msg[1] == 0x1)
				{
					analogWrite(MOTOR_LEFT, 255 - msg[2]);
					switch_led();
				}
				else if (msg[1] == 0x2)
				{
					analogWrite(CUTTER, 255 - msg[2]);
					switch_led();
				}
			} else if (msg[0] == 0x3) {
				if (msg[1] == 0x0)
				{
					digitalWrite(RELAY_RIGHT, msg[2] ? HIGH : LOW);
					switch_led();
				}
				else if (msg[1] == 0x1)
				{
					digitalWrite(RELAY_LEFT, msg[2] ? HIGH : LOW);
					switch_led();
				}
			} else if (msg[0] == 0x4) {
				val = -1;
				if (msg[1] == 0x0)
					val = analogRead(RANGE_SENSOR);
				else if (msg[1] == 0x1)
					val = analogRead(MOIST_SENSOR);
				else if (msg[1] == 0x2)
					val = analogRead(VOLTAGE_SENSOR);
				else if (msg[1] == 0x3)
					val = analogRead(BWF_SENSOR_RIGHT);
				else if (msg[1] == 0x4)
					val = analogRead(BWF_SENSOR_LEFT);
				
				if ( val > -1 )
				{
					msg[0] = 0x1;
			                msg[2] = val >> 8;
                			msg[3] = val & 0xff;
					acc.write(msg, 4);
				}
			}
		}
	} else {
		// reset outputs to default values on disconnect
		analogWrite(MOTOR_RIGHT, 255);
		analogWrite(MOTOR_LEFT, 255);
		analogWrite(CUTTER, 255);
		digitalWrite(RELAY_RIGHT, LOW);
		digitalWrite(RELAY_LEFT, LOW);
	}
}
 
