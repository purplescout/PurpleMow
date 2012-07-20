
#include <sys/socket.h>
#include <netdb.h>

#include <stdio.h>

#include <stdlib.h>  // exit
#include <string.h>

#include "modules.h"

#include "io.h"
#include "commands.h"

#define PORT "35424"
#define SERVER "localhost"

/**
 * @defgroup io_net Network IO
 * Network implementation for the IO Interface.
 *
 * @ingroup io
 */

/**
 * io_net
 *
 * @ingroup io_net
 */
struct io_net
{
    int sock;
    int debug;
};

// functions
static error_code io_start();
static error_code io_stop();

static struct io_net this = { .sock = -1, .debug = 0 };

/**
 * Initialize the net io.
 *
 * @ingroup io_net
 *
 * @return          Success status
 */
error_code io_init()
{
    module_register_to_phase(phase_START, io_start);
    module_register_to_phase(phase_STOP, io_stop);
    return err_OK;
}

static error_code io_start()
{
    struct addrinfo hints;
    struct addrinfo* addr;
    int result;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    result = getaddrinfo(SERVER, PORT, &hints, &addr);
    if ( result != 0 ) {
        perror("Failed to get addr info");
        exit(1);
    }

    this.sock = socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol);
    if ( this.sock < 0 ) {
        perror("Failed to create socket");
        exit(1);
    }

    result = connect(this.sock, addr->ai_addr, addr->ai_addrlen);
    if ( result < 0 ) {
        perror("Failed to connect");
        exit(1);
    }

    freeaddrinfo(addr);

    return err_OK;
}

static error_code io_stop()
{
    if ( this.sock != -1 ) {
        close(this.sock);
        this.sock = -1;
    }

    return err_OK;
}

/**
 * Send a message over network to I/O board.
 *
 * @ingroup io_net
 *
 * @param[in] msg       Message to send
 * @param[in] length    Length of message
 *
 * @return              Success status
 */
static error_code net_send_command(uint8_t* msg, int length)
{
    int result;

    if ( this.sock == -1 )
        return err_NOT_INITIALIZED;

    if ( length != MAX_MSG_SIZE )
        return err_WRONG_ARGUMENT;

    result = write(this.sock, msg, length);

    if ( result < 0 ) {
        perror("Failed to write command");
        return err_NETWORK;
    }

    return err_OK;
}

/**
 * Read a message from net.
 *
 * @ingroup io_net
 *
 * @param[out] msg      Buffer to write to
 * @param[in]  length   Length of buffer
 *
 * @return              Success status
 */
static error_code net_read_data(uint8_t* msg, int length)
{
    int result;

    if ( this.sock == -1 )
        return err_NOT_INITIALIZED;

    if ( length != MAX_MSG_SIZE )
        return err_WRONG_ARGUMENT;

    result = read(this.sock, msg, length);

    if ( result < 0 ) {
        perror("Failed to read command");
        return err_NETWORK;
    }

    return err_OK;
}

/**
 * Send a command to a motor.
 *
 * @ingroup io_net
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
        net_send_command( msg, 4 );
    }

    return error;
}

/**
 * Send a command to a relay.
 *
 * @ingroup io_net
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
        net_send_command( msg, 4 );
    }

    return error;
}

/**
 * Read a value from the I/O board.
 *
 * @ingroup io_net
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
        net_send_command( msg, 4 );
        net_read_data( msg, 4 );
        *value = (msg[2] << 8) + msg[3];
        if ( this.debug )
            printf("io read: %s %d\n", cmd, *value);
    }

    return error;
}
