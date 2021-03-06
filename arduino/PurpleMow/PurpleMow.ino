

#include <Wire.h>
#include <Servo.h>

#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#include "commands.h"

#define  MOTOR_RIGHT_PWM        8
#define  MOTOR_LEFT_PWM         9
#define  CUTTER                 11

#define  MOTOR_RIGHT_A          22
#define  MOTOR_RIGHT_B          24
#define  MOTOR_LEFT_A           28
#define  MOTOR_LEFT_B           30

#define  RANGE_SENSOR_LEFT      A2
#define  RANGE_SENSOR_RIGHT     A3
#define  BWF_SENSOR_CENTER      A4
#define  VOLTAGE_SENSOR         A5
#define  MOIST_SENSOR           A7
#define  CUTTER_OVERCURRENT_SENSOR A8

#define I2C_SDA                 20
#define I2C_SCL                 21

#define ONBOARD_LED             13

#define I2C_ADDRESS             0x35

// private functions
void i2c_receive(int bytes);
void i2c_request();
int process_command(byte* msg, int length);

// global variables
AndroidAccessory acc("PurpleScout AB",
"PurpleMow",
"PurpleMow Arduino Board",
"1.0",
"http://www.purplescout.se/blog/category/purplemow",
"0000000012345678");
byte i2c_msg[MAX_MSG_SIZE];
int i2c_data;
int i2c_read_pos;
byte sensorData[12];

void setup() {
  Serial.begin(9600);
  Serial.print("\r\nStart");
  pinMode(MOTOR_RIGHT_PWM, OUTPUT);   // sets the pin as output
  pinMode(MOTOR_LEFT_PWM, OUTPUT);   // sets the pin as output
  pinMode(CUTTER, OUTPUT);   // sets the pin as output
  pinMode(MOTOR_RIGHT_A, OUTPUT);
  pinMode(MOTOR_RIGHT_B, OUTPUT);
  pinMode(MOTOR_LEFT_A, OUTPUT);
  pinMode(MOTOR_LEFT_B, OUTPUT);
  pinMode(RANGE_SENSOR_RIGHT, INPUT);
  pinMode(RANGE_SENSOR_LEFT, INPUT);
  pinMode(MOIST_SENSOR, INPUT);
  pinMode(VOLTAGE_SENSOR, INPUT);
  pinMode(BWF_SENSOR_CENTER, INPUT);
  pinMode(CUTTER_OVERCURRENT_SENSOR, INPUT);

  acc.powerOn();

  Wire.begin(I2C_ADDRESS);
  Wire.onReceive(i2c_receive);
  Wire.onRequest(i2c_request);
  i2c_data = 0;
  i2c_read_pos = 0;
}
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
      } else if ( to_write == 12 ){
        //Request for all sensor values
        acc.write(sensorData, to_write);
      }
    }
  } 
  else {
    // reset outputs to default values on disconnect
    analogWrite(MOTOR_RIGHT_PWM, 0);
    analogWrite(MOTOR_LEFT_PWM, 0);
    analogWrite(CUTTER, 255);
    digitalWrite(MOTOR_RIGHT_A, LOW);
    digitalWrite(MOTOR_RIGHT_B, LOW);
    digitalWrite(MOTOR_LEFT_A, LOW);
    digitalWrite(MOTOR_LEFT_B, LOW);

  }
}


void getSensorData() {
  int16_t val;
  val = -1;
  val = analogRead(RANGE_SENSOR_RIGHT);
  if ( val > -1 ) {
    sensorData[2] = val >> 8;
    sensorData[3] = val & 0xff;
    val = -1;
  }
  val = analogRead(RANGE_SENSOR_LEFT);
  if ( val > -1 ) {
    sensorData[4] = val >> 8;
    sensorData[5] = val & 0xff;
    val = -1;
  }
  val = analogRead(BWF_SENSOR_CENTER);
  if ( val > -1 ) {
    sensorData[6] = val >> 8;
    sensorData[7] = val & 0xff;
    val = -1;
  }
  val = analogRead(VOLTAGE_SENSOR);
  if ( val > -1 ) {
    sensorData[8] = val >> 8;
    sensorData[9] = val & 0xff;
    val = -1;
  }
  val = analogRead(CUTTER_OVERCURRENT_SENSOR);
  if ( val > -1 ) {
    sensorData[10] = val >> 8;
    sensorData[11] = val & 0xff;
    val = -1;
  }

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
      analogWrite(MOTOR_RIGHT_PWM, msg[2]);
      result = 0;
    }
    else if (msg[1] == CMD_MOTOR_LEFT)
    {
      analogWrite(MOTOR_LEFT_PWM, msg[2]);
      result = 0;
    }
    else if (msg[1] == CMD_CUTTER)
    {
      analogWrite(CUTTER, 255 - msg[2]);
      result = 0;
    }
  } 
  else if (msg[0] == CMD_RELAY) {
    if (msg[1] == CMD_RELAY_RIGHT)
    {
      digitalWrite(MOTOR_RIGHT_A, msg[2] ? HIGH : LOW);
      digitalWrite(MOTOR_RIGHT_B, msg[2] ? LOW : HIGH);
      result = 0;
    }
    else if (msg[1] == CMD_RELAY_LEFT)
    {
      digitalWrite(MOTOR_LEFT_A, msg[2] ? HIGH : LOW);            
      digitalWrite(MOTOR_LEFT_B, msg[2] ? LOW : HIGH);
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
      
    case CMD_GET_ALL_SENSORS:
      sensorData[0] = CMD_SEND;
      sensorData[1] = msg[1];
      result = 12;
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

void i2c_receive(int bytes)
{
  int i = 0;

  if ( bytes >= MAX_MSG_SIZE + 1 )
  {
    char c;
    c = Wire.read();

    if ( c != CMD_I2C_MAGIC )
    {
      return;
    }

    while ( --bytes > 0 )
    {
      c = Wire.read();
      if ( i < MAX_MSG_SIZE )
      {
        i2c_msg[i] = c;
      }
    }
    char buff[256];
    snprintf(buff, sizeof(buff),"\r\nGott!  %02x %02x %02x %02x", i2c_msg[0], i2c_msg[1], i2c_msg[2], i2c_msg[3] );
    Serial.print(buff);
    i2c_data = process_command(i2c_msg, sizeof(i2c_msg));
    i2c_read_pos = 0;
  }
}

void i2c_request()
{
  if ( i2c_read_pos < i2c_data )
  {
    Wire.write((uint8_t)i2c_msg[i2c_read_pos]);
    i2c_read_pos++;
  }
  else
  {
    Wire.write((uint8_t)0);
  }
}




