#ifndef COMMANDS_H
#define COMMANDS_H

// message[0]
#define CMD_SEND                0x1
#define CMD_WRITE               0x2
#define CMD_RELAY               0x3
#define CMD_READ                0x4

// message[0] == CMD_WRITE
// message[1]
#define CMD_MOTOR_RIGHT         0x0
#define CMD_MOTOR_LEFT          0x1
#define CMD_CUTTER              0x2

// message[0] == CMD_RELAY
// message[1]
#define CMD_RELAY_RIGHT         0x0
#define CMD_RELAY_LEFT          0x1

// message[0] == CMD_READ
// message[1]
#define CMD_RANGE_SENSOR_LEFT        0x0
#define CMD_RANGE_SENSOR_RIGHT        0x1
#define CMD_VOLTAGE_SENSOR      0x2
#define CMD_BWF_LEFT_SENSOR     0x3
#define CMD_BWF_RIGHT_SENSOR    0x4
#define CMD_MOIST_SENSOR        0x5
#define CMD_I2C_MAGIC           0x54

#define MAX_MSG_SIZE            4

#endif // COMMANDS_H
