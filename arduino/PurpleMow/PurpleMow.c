

//#include <Wire.h>
//#include <Servo.h>

//#include <Max3421e.h>
//#include <Usb.h>
//#include <AndroidAccessory.h>

/*

  Project uses the LGPLv3 wiringPi lib

  https://projects.drogon.net/raspberry-pi/wiringpi/functions/

*/

#include <wiringPi.h>
#include <softPwm.h>

#include <stdint.h>
#include <stdio.h>
#include <string.h>

#include "commands.h"
#include "socket.h"

// wiringPi GPIO pin numbering
#define  MOTOR_RIGHT_PWM        6
#define  MOTOR_LEFT_PWM         7
#define  CUTTER                 1

#define  MOTOR_RIGHT_A          2
#define  MOTOR_RIGHT_B          3
#define  MOTOR_LEFT_A           4
#define  MOTOR_LEFT_B           5

#define HIGH 1
#define LOW 0

//Remove define for DRY_RUN to enable actual IO calls

#define DRY_RUN 1

int process_command(uint8_t* msg, int length);


void setup() {

    /* Sets up GPIO using wiringPi pin numbering */
    wiringPiSetup();

    if(softPwmCreate(MOTOR_RIGHT_PWM,0,100) != 0)
    {
	printf("Error setting up MOTOR_RIGHT_PWM\n\r");
    }

    if(softPwmCreate(MOTOR_LEFT_PWM,0,100) != 0)
    {
	printf("Error setting up MOTOR_LEFT_PWM\n\r");
    }
	
    pinMode(CUTTER, PWM_OUTPUT);   // sets the pin as output
    pinMode(MOTOR_RIGHT_A, OUTPUT);
    pinMode(MOTOR_RIGHT_B, OUTPUT);
    pinMode(MOTOR_LEFT_A, OUTPUT);
    pinMode(MOTOR_LEFT_B, OUTPUT);
}

// Byte 1: 2 för att prata med motorer, 3 för att prata med relän
// Byte 2: 0 för höger, 1 för vänster, 2 för klippmotor

/* Do we have a led to control? 
 *
void switch_led()
{


    static int led_on = HIGH;

    led_on = led_on == LOW ? HIGH : LOW;

    digitalWrite(ONBOARD_LED, led_on);

}
*/

void main() {
    uint8_t err;
    uint8_t idle;
    int len;
    int i,x,y;

    uint8_t b;
    char c0;

    static uint8_t count = 0;
    char buffer[MAX_MSG_SIZE];
    uint8_t msg[MAX_MSG_SIZE];
    long touchcount;
    int to_write;
    uint8_t running=1;
    uint8_t connected=1;
    char * socketpath = "/var/run/purplemow.sock";

    #ifndef DRY_RUN
    setup();
    #endif
  

    create_server_socket(socketpath, strlen(socketpath));
    
    
    while (running) {
	printf("Awaiting cmd\n");
	fflush(stdout);
        
        read_from_socket(buffer,MAX_MSG_SIZE);

	msg[0]=buffer[0]-48;
        msg[1]=buffer[1]-48;
        msg[2]=buffer[2]-48;
        msg[3]=buffer[3]-48;
        
        printf("%d %d %d\n\r", msg[0],msg[1],msg[2],msg[3]);

        if(connected)
	{

	  len=strlen(msg);
          if (len > 0) {
              // assumes only one command per packet
              to_write = process_command(msg, sizeof(msg));
              if ( to_write > 0 )
              {
                  printf("Message: %s, Length: %d", msg, to_write);
              }
          }
	}
        else {
        // reset outputs to default values on disconnect
          #ifdef DRY_RUN 
	  printf("Disconnected\n\r");
          #else
          softPwmWrite(MOTOR_RIGHT_PWM, 0);
	  softPwmWrite(MOTOR_LEFT_PWM, 0);
	  pwmWrite(CUTTER, 255);
          digitalWrite(MOTOR_RIGHT_A, LOW);
          digitalWrite(MOTOR_RIGHT_B, LOW);
          digitalWrite(MOTOR_LEFT_A, LOW);
          digitalWrite(MOTOR_LEFT_B, LOW);
	  #endif
	  running=0;
	
        }
    }

    close_socket();
}

// returns the number of bytes written to msg
// returns -1 on unknown command
int process_command(uint8_t* msg, int length)
{
    int result = -1;

    if (length < MAX_MSG_SIZE) {
        return -1;
    }

	
    if (msg[0] == CMD_WRITE) {
        if (msg[1] == CMD_MOTOR_RIGHT)
        {
	    #ifdef DRY_RUN
               printf("MOTOR_RIGHT_PWM %d\n",msg[2]);
            #else
               softPwmWrite(MOTOR_RIGHT_PWM, msg[2]);
            #endif
            result = 0;
        }
        else if (msg[1] == CMD_MOTOR_LEFT)
        {
            #ifdef DRY_RUN
               printf("MOTOR_LEFT_PWM %d\n", msg[2]);
            #else
	       softPwmWrite(MOTOR_LEFT_PWM, msg[2]);
            #endif
            result = 0;
        }
        else if (msg[1] == CMD_CUTTER)
        {
            #ifdef DRY_RUN
               printf("CUTTER %d\n", 255-msg[2]);
            #else
	       pwmWrite(CUTTER, 255 - msg[2]);
            #endif
            result = 0;
        }
    } else if (msg[0] == CMD_RELAY) {
        if (msg[1] == CMD_RELAY_RIGHT)
        {
            #ifdef DRY_RUN
               printf("CMD_RELAY_RIGHT\n\r");           
            #else           
                digitalWrite(MOTOR_RIGHT_A, msg[2] ? HIGH : LOW);
                digitalWrite(MOTOR_RIGHT_B, msg[2] ? LOW : HIGH);
            #endif
            result = 0;
        }
        else if (msg[1] == CMD_RELAY_LEFT)
        {
            #ifdef DRY_RUN
                printf("CMD_RELAY_LEFT\n\r");            
            #else
                digitalWrite(MOTOR_LEFT_A, msg[2] ? HIGH : LOW);            
                digitalWrite(MOTOR_LEFT_B, msg[2] ? LOW : HIGH);
            #endif
            result = 0;
        }
    } else if (msg[0] == CMD_READ) {
        int16_t val;
        val = -1;
        switch (msg[1])
        
		/* Sensor values read from /proc? */{
            case CMD_RANGE_SENSOR_RIGHT:
                printf("val = analogRead(RANGE_SENSOR_RIGHT);\n\r");
                break;
            case CMD_RANGE_SENSOR_LEFT:
                printf("val = analogRead(RANGE_SENSOR_LEFT);\n\r");
                break;
            case CMD_MOIST_SENSOR:
		printf("val = analogRead(MOIST_SENSOR);\n\r");
                break;

            case CMD_VOLTAGE_SENSOR:
                printf("val = analogRead(VOLTAGE_SENSOR);\n\r");
                break;

            case CMD_BWF_LEFT_SENSOR:
                printf("val = analogRead(BWF_SENSOR_RIGHT);\n\r");
                break;

            case CMD_BWF_RIGHT_SENSOR:
                printf("val = analogRead(BWF_SENSOR_LEFT);\n\r");
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



