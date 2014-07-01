#include <Wire.h>
#include <Servo.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#include "commands.h"

#define  MOTOR_RIGHT_PWM_1        7
#define  MOTOR_RIGHT_PWM_2        8
#define  MOTOR_LEFT_PWM_1         9
#define  MOTOR_LEFT_PWM_1         10
#define  CUTTER_PWM               11

#define  MOTOR_RIGHT_DIR            23
#define  MOTOR_LEFT_DIR             25
#define  MOTOR_CUTTER_DIR           27


#define  PUSH_BUTTON_START_MOW  29
#define  BUMPER                 33
#define  RANGE_SENSOR_LEFT      A2
#define  RANGE_SENSOR_RIGHT     A3
#define  BWF_SENSOR_CENTER      A4
#define  VOLTAGE_SENSOR         A5
#define  MOIST_SENSOR           A7
#define  CUTTER_OVERCURRENT_SENSOR A8

#define ONBOARD_LED             13

// private functions
int process_command(byte* msg, int length);
void checkBWF();
void zeroPadByteArray();
void setupCounter();
void getCutterRPM();

// global variables
AndroidAccessory acc("PurpleScout AB",
"PurpleMow",
"PurpleMow Arduino Board",
"1.0",
"http://www.purplescout.se/blog/category/purplemow",
"0000000012345678");
byte sensorData[16]; // Holds all sensor data

const int insideByte 					= 107; // 107 is inside fence   
const int outsideByte 					= 171; // 171 is outside fence
const byte InsideBWF                    = 0;
const byte OutsideBWF                   = 1;
const byte UnknownBWF                   = 2;

byte oldBWFReading = 0;
unsigned long timer = millis();
long timeOut = 40000;
unsigned long overflowCount;
unsigned long startTime;

void setup() {
  Serial1.begin(9600, SERIAL_8E1);
  Serial.begin(9600);
  Serial.print("\r\nStart PurpleMow Arduino");
  pinMode(MOTOR_RIGHT_PWM_1, OUTPUT);   // sets the pin as output
  pinMode(MOTOR_RIGHT_PWM_2, OUTPUT);   // sets the pin as output
  pinMode(MOTOR_LEFT_PWM_1, OUTPUT);   // sets the pin as output
  pinMode(MOTOR_LEFT_PWM_2, OUTPUT);   // sets the pin as output
  pinMode(CUTTER_PWM, OUTPUT);   // sets the pin as output
//  pinMode(MOTOR_RIGHT_DIR, OUTPUT);
//  pinMode(MOTOR_LEFT_DIR, OUTPUT);
  pinMode(MOTOR_CUTTER_DIR, OUTPUT);
  pinMode(RANGE_SENSOR_RIGHT, INPUT);
  pinMode(RANGE_SENSOR_LEFT, INPUT);
  pinMode(MOIST_SENSOR, INPUT);
  pinMode(VOLTAGE_SENSOR, INPUT);
  pinMode(BWF_SENSOR_CENTER, INPUT);
  pinMode(CUTTER_OVERCURRENT_SENSOR, INPUT);
  pinMode(BUMPER, INPUT);
  digitalWrite(BUMPER, HIGH);  // Enable pull-up resistor
  pinMode(PUSH_BUTTON_START_MOW, INPUT); 
  digitalWrite(PUSH_BUTTON_START_MOW, HIGH);

  acc.powerOn();

  zeroPadByteArray();

  setupCounter();

}

ISR (TIMER5_OVF_vect)
{
  ++overflowCount;               // count number of Counter1 overflows  
}  // end of TIMER5_OVF_vect


// Byte 1: 2 för att prata med motorer, 3 för att prata med relän
// Byte 2: 0 för höger, 1 för vänster, 2 för klippmotor
void loop() {
  byte msg[MAX_MSG_SIZE];

  int to_write;

  if (acc.isConnected()) {
    //Retrieve sensor values from all sensors at once
    getSensorData();
    int len = acc.read(msg, 3, 1);
    if (len > 0) {
      // assumes only one command per packet
      to_write = process_command(msg, sizeof(msg));

      if ( to_write > 0 && to_write <= 4) {
        //Request for single sensor or setting an output
        acc.write(msg, to_write);
      } else if ( to_write == 16 ) {
        //Request for all sensor values
        acc.write(sensorData, to_write);
      }
    }
  } 
  else {
    // reset outputs to default values on disconnect
    digitalWrite(MOTOR_RIGHT_PWM_1, LOW);
    digitalWrite(MOTOR_LEFT_PWM_1, LOW);
    digitalWrite(MOTOR_RIGHT_PWM_2, LOW);
    digitalWrite(MOTOR_LEFT_PWM_2, LOW);
    analogWrite(CUTTER_PWM, 0);
    digitalWrite(MOTOR_CUTTER_DIR, HIGH);
//    digitalWrite(MOTOR_RIGHT_DIR, HIGH);
//    digitalWrite(MOTOR_LEFT_DIR, LOW);

  }
}


void getSensorData() {
  int16_t val;
  val = -1;

  val = analogRead(VOLTAGE_SENSOR);
  if ( val > -1 ) {
    sensorData[8] = val >> 8;
    sensorData[9] = val & 0xff;
    val = -1;
  }

  val = digitalRead(BUMPER);
  if ( val == HIGH ) {
    sensorData[12] = (byte) 255;
  } 
  else {
    sensorData[12] = (byte) 0;
  }
  val = -1;

  val = digitalRead(PUSH_BUTTON_START_MOW);
  if ( val == HIGH ) {
    sensorData[13] = (byte) 255;
  } 
  else {
    sensorData[13] = (byte) 0;
  }

  //Get BWF data
  checkBWF();

  //Check that the cutter motor is running
  getCutterRPM();

}

// returns the number of bytes written to msg
// returns -1 on unknown command
int process_command(byte* msg, int length)
{
  int result = 0;

  if (length < MAX_MSG_SIZE) {
    return -1;
  }

  if (msg[0] == CMD_WRITE) {
    if (msg[1] == CMD_MOTOR_RIGHT)
    {
      if(msg[2] > 0) {
        digitalWrite(MOTOR_RIGHT_PWM, HIGH);
      } 
      else {
        digitalWrite(MOTOR_RIGHT_PWM, LOW);
      }
      result = 0;
    }
    else if (msg[1] == CMD_MOTOR_LEFT)
    {
      if(msg[2] > 0) {
        digitalWrite(MOTOR_LEFT_PWM, HIGH);
      } 
      else {
        digitalWrite(MOTOR_LEFT_PWM, LOW);
      }
      result = 0;
    }
    else if (msg[1] == CMD_CUTTER)
    {
      analogWrite(CUTTER_PWM, msg[2]);
      result = 0;
    }
  } 
  else if (msg[0] == CMD_RELAY) {
    Serial.print("\r\nCommand RELAY received\r\n");
    Serial.print(msg[0]);
    Serial.print("\t"); 
    Serial.print(msg[1]);
    Serial.print("\t"); 
    Serial.print(msg[2]);
    Serial.print("\r\n");   
    if (msg[1] == CMD_RELAY_RIGHT)
    {
      digitalWrite(MOTOR_RIGHT_DIR, msg[2] ? LOW : HIGH);
      result = 0;
    }
    else if (msg[1] == CMD_RELAY_LEFT)
    {
      digitalWrite(MOTOR_LEFT_DIR, msg[2] ? HIGH : LOW);            
      result = 0;
    }
  } 
  else if (msg[0] == CMD_READ) {
    int16_t val;
    val = -1;
    switch (msg[1])
    {
    case CMD_RANGE_SENSOR_RIGHT:
      val = analogRead(RANGE_SENSOR_RIGHT);
      break;
    case CMD_RANGE_SENSOR_LEFT:
      val = analogRead(RANGE_SENSOR_LEFT);
      break;
    case CMD_MOIST_SENSOR:
      val = analogRead(MOIST_SENSOR);
      break;

    case CMD_VOLTAGE_SENSOR:
      val = analogRead(VOLTAGE_SENSOR);
      break;

    case CMD_BWF_SENSOR:
      val = analogRead(BWF_SENSOR_CENTER);
      break;

    case CMD_BUMPER_SENSOR:
      val = digitalRead(BUMPER);
      break;

    case CMD_GET_ALL_SENSORS:
      sensorData[0] = CMD_SEND;
      sensorData[1] = msg[1];
      result = 16;
      break;
    }

    if ( val > -1 )
    {
      msg[0] = CMD_SEND;
      // msg[1] the same as requested
      msg[2] = val >> 8;
      msg[3] = val & 0xff;
      result = 4;
    }
  } 

  return result;
}

/*
	 BWF sends data over serial interface. If there is a signal it will be sent every 30 millisecond.
 	 Jumper Inne Ute
 	 A 107 171
 	 B 215 87
 	 C 207 111
 	 D 91 109
 	 E 183 219 
 */
void checkBWF() {
  if (Serial1.available()) {
    byte bwfReading =  (byte) Serial1.read();
    sensorData[14] = bwfReading;
  }
}

void getCutterRPM() {
  unsigned int timer5CounterValue;
  timer5CounterValue = TCNT5;  // see datasheet, (accessing 16-bit registers)

  // calculate total count
  unsigned long pulsesTotalCount = (overflowCount << 16) + timer5CounterValue;  // each overflow is 65536 more
  //Hämta nuvarande systemtid
  unsigned long duration = millis() - startTime;
  float freq = 1000 * pulsesTotalCount / duration / 24;
  unsigned int freqAsInt = (int) freq;
  sensorData[6] = pulsesTotalCount >> 8;
  sensorData[7] = pulsesTotalCount & 0xff;

  //reset counter
  TCNT5 = 0;
  overflowCount = 0;
  startTime = millis();

}

void zeroPadByteArray() {
  for(int i = 0; i < sizeof(sensorData); i++) {
    sensorData[i] = 0;
  }
}

void setupCounter() {
  // http://www.gammon.com.au/forum/?id=11504
  overflowCount = 0;            // no overflows yet
  TCCR5A = 0;             
  TCCR5B = 0;
  TCNT5 = 0;      // Counter reset

  // Timer 5 - counts events on pin D47
  TIMSK5 = bit (TOIE1);   // interrupt on Timer 5 overflow
  
  // Reset prescaler
  GTCCR = bit (PSRASY);
  
  // External clock source on T5 pin (D47). Clock on rising edge.
  TCCR5B =  bit (CS50) | bit (CS51) | bit (CS52);
  
  startTime = millis();

}

