
#include <stdio.h>

#include <stdlib.h>  // exit
#include <string.h>
#include <stdint.h>

#include "modules.h"

#include "io.h"
#include "commands.h"

/**
 * @defgroup io_data IO
 *
 * @ingroup io
 */

/**
 * io
 *
 * @ingroup io_data
 */
struct io_data
{
    int debug;
};

// functions
static error_code io_start();
static error_code io_stop();
static error_code io_start();
static error_code command_i2c_read(char *args);

static struct io_data this = { .debug = 0 };

/**
 * Initialize the net io.
 *
 * @ingroup io_data
 *
 * @return          Success status
 */
error_code io_init()
{
    module_register_to_phase(phase_START, io_start);

    io_transport_init();

    return err_OK;
}

static error_code io_start()
{
    cli_register_command("i2c_read", command_i2c_read);

    return err_OK;
}

/**
 * Send a command to a motor.
 *
 * @ingroup io_data
 *
 * @param[in] direction     Which motor to send the command to
 * @param[in] command       Command to send
 * @param[in] speed         Speed to send
 *
 * @return                  Success status
 */
error_code io_command_motor(enum direction direction, enum command command, int speed)
{
    int error = err_OK;
    uint8_t msg[4] = { 0 };

    char*   cmd = "";

    msg[0] = CMD_WRITE;

    switch ( direction ) {
        case direction_left:
            cmd = "left";
            msg[1] = CMD_MOTOR_LEFT;
            break;
        case direction_right:
            cmd = "right";
            msg[1] = CMD_MOTOR_RIGHT;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    speed = speed > 255 ? 255 : speed;

    switch ( command ) {
        case command_start:
            msg[2] = speed;
            break;
        case command_stop:
            msg[2] = 0;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) ) {
        if ( this.debug )
            printf("io motor: %s %d\n", cmd, msg[2]);
        io_transport_send_command( msg, 4 );
    }

    return error;
}

/**
 * Send a command to a relay.
 *
 * @ingroup io_data
 *
 * @param[in] direction     Which relay to send the command to
 * @param[in] command       Command to send
 *
 * @return                  Success status
 */
error_code io_command_relay(enum direction direction, enum direction command)
{
    int error = err_OK;
    uint8_t msg[4] = { 0 };

    msg[0] = CMD_RELAY;

    char*   cmd[2];
    cmd[0] = "";
    cmd[1] = "";

    switch ( direction ) {
        case direction_left:
            cmd[0] = "left";
            msg[1] = CMD_RELAY_LEFT;
            break;
        case direction_right:
            cmd[0] = "right";
            msg[1] = CMD_RELAY_RIGHT;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    switch ( command ) {
        case direction_forward:
            cmd[1] = "forward";
            msg[2] = 1;
            break;
        case direction_backward:
            cmd[1] = "backward";
            msg[2] = 0;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) ) {
        if ( this.debug )
            printf("relay: %s %s\n", cmd[0], cmd[1]);
        io_transport_send_command( msg, 4 );
    }

    return error;
}

/**
 * Read a value from the I/O board.
 *
 * @ingroup io_data
 *
 * @param[in] sensor        Sensor to read
 * @param[out] value        Read value
 *
 * @return                  Success status
 */
error_code io_command_read(enum sensor sensor, int *value)
{
    int error = err_OK;
    uint8_t msg[4] = { 0 };

    msg[0] = CMD_READ;

    char*   cmd = "";

    switch ( sensor ) {
        case sensor_range_left:
            cmd = "range left";
            msg[1] = CMD_RANGE_SENSOR_LEFT;
            break;
        case sensor_range_right:
            cmd = "range right";
            msg[1] = CMD_RANGE_SENSOR_RIGHT;
            break;
        case sensor_moist:
            cmd = "moist";
            msg[1] = CMD_MOIST_SENSOR;
            break;
        case sensor_voltage:
            cmd = "voltage";
            msg[1] = CMD_VOLTAGE_SENSOR;
            break;
        case sensor_bwf_left:
            cmd = "BWF right";
            msg[1] = CMD_BWF_LEFT_SENSOR;
            break;
        case sensor_bwf_right:
            cmd = "BWF right";
            msg[1] = CMD_BWF_RIGHT_SENSOR;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) ) {
        io_transport_send_command( msg, 4 );
        io_transport_read_data( msg, 4 );
        *value = (msg[2] << 8) + msg[3];
        if ( this.debug )
            printf("io read: %s %d\n", cmd, *value);
    }

    return error;
}

/**
 * The command <b>i2c_read</b>, readable values from I2C.
 *
 * @ingroup io_i2c
 *
 * @param[in] args  Arguments
 *
 * @return          Success status
 */
static error_code command_i2c_read(char *args)
{
    int value = 0;
    error_code result = err_WRONG_ARGUMENT;
    if ( strcmp("range", args) == 0 ) {
        result = io_command_read(sensor_range_left, &value);
    } else if ( strcmp("voltage", args) == 0 ) {
        result = io_command_read(sensor_voltage, &value);
    } else if ( strcmp("moist", args) == 0 ) {
        result = io_command_read(sensor_moist, &value);
    } else if ( strcmp("bwf_l", args) == 0 ) {
        result = io_command_read(sensor_bwf_left, &value);
    } else if ( strcmp("bwf_r", args) == 0 ) {
        result = io_command_read(sensor_bwf_right, &value);
//TODO
#if 0
    } else if ( strcmp("bwf_ref", args) == 0 ) {
        result = io_command_read(sensor_bwf_reference, &value);
#endif
    } else {
        result = err_WRONG_ARGUMENT;
        printf("Valid arguments: "
                "range, bwf_l, bwf_r, bwf_ref"
                ", moist"
                "\n");
    }
    if ( SUCCESS(result) ) {
        printf("%d\n", value);
    }

    return err_OK;
}

