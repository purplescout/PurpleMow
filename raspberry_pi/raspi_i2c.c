
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdint.h>
#include "include/linux/i2c-dev.h"
#ifdef SIMULATOR
#include <string.h>
#endif // SIMULATOR

#include <stdlib.h>  // exit

#include "raspi_io.h"
#include "../arduino/PurpleMow/commands.h"

#define I2C_DEVICE "/dev/i2c-2"
//#define I2C_DEVICE "/dev/i2c-4"
#define I2C_ADDRESS 0x35

/**
 * @defgroup raspi_i2c Raspberry PI I2C
 * I2C implementation for the IO Interface.
 *
 * @ingroup io
 */

// cli commands
static error_code command_i2c(char *args);
static error_code command_i2c_read(char *args);

// i2c
static error_code i2c_send_command(uint8_t* msg, int length);
static error_code i2c_read_data(uint8_t* msg, int length);

/**
 * i2c
 *
 * @ingroup raspi_i2c
 */
struct i2c
{
    int fd;
    struct timeval last_command;
    int debug;
#ifdef SIMULATOR
    int timed;
    uint16_t range;
    uint16_t moist;
    uint16_t voltage;
    uint16_t bwf_l;
    uint16_t bwf_r;
    uint16_t bwf_ref;
#endif // SIMULATOR
};

static struct i2c this = { .fd = -1, .debug = 0 };

/**
 * Initialize the i2c.
 *
 * @ingroup raspi_i2c
 *
 * @return          Success status
 */
error_code purple_io_init()
{
#ifdef SIMULATOR
#else
    int res;

    this.fd = open(I2C_DEVICE, O_RDWR);

    if ( this.fd < 0 ) {
        perror("opening i2c device");
        return err_OPEN_DEVICE;
    }

    res = ioctl(this.fd, I2C_SLAVE, I2C_ADDRESS);

    if ( res < 0 ) {
        perror("setting i2c device to slave");
        return err_CONFIGURE_DEVICE;
    }

    gettimeofday(&this.last_command, NULL);
#endif // SIMULATOR

    cli_register_command("i2c", command_i2c);
    cli_register_command("i2c_read", command_i2c_read);

    return err_OK;
}

/**
 * The command <b>i2c</b>, debugging options for I2C
 *
 * @ingroup raspi_i2c
 *
 * @param[in] args  Arguments
 *
 * @return          Success status
 */
static error_code command_i2c(char *args)
{
    if ( strcmp("debug", args) == 0 ) {
        printf("Enabled i2c debugging\n");
        this.debug = 1;
    } else if ( strcmp("nodebug", args) == 0 ) {
        printf("Disabled i2c debugging\n");
        this.debug = 0;
#ifdef SIMULATOR
    } else if ( strcmp("timed", args) == 0 ) {
        this.timed = 1;
    } else if ( strcmp("untimed", args) == 0 ) {
        this.timed = 0;
    } else if ( strncmp("range", args, strlen("range")) == 0 ) {
        this.range = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.range = 0;
        }
    } else if ( strncmp("voltage", args, strlen("voltage")) == 0 ) {
        this.voltage = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.voltage = 0;
        }
    } else if ( strncmp("moist", args, strlen("moist")) == 0 ) {
        this.moist = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.moist = 0;
        }
    } else if ( strncmp("bwf_l", args, strlen("bwf_l")) == 0 ) {
        this.bwf_l = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.bwf_l = 0;
        }
    } else if ( strncmp("bwf_r", args, strlen("bwf_r")) == 0 ) {
        this.bwf_r = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.bwf_r = 0;
        }
    } else if ( strncmp("bwf_ref", args, strlen("bwf_ref")) == 0 ) {
        this.bwf_ref = cli_read_int(args);
        if ( this.timed ) {
            sleep(1);
            this.bwf_ref = 0;
        }
#endif // SIMULATOR
    } else {
        printf("Valid arguments: debug, nodebug"
#ifdef SIMULATOR
                ", range, bwf_l, bwf_r, bwf_ref"
                ", moist, timed, untimed"
#endif // SIMULATOR
                "\n");
    }
    return err_OK;
}

/**
 * The command <b>i2c_read</b>, modify readable values in simulator mode.
 *
 * @ingroup raspi_i2c
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
        result = io_command_read(sensor_range, &value);
    } else if ( strcmp("voltage", args) == 0 ) {
        result = io_command_read(sensor_voltage, &value);
    } else if ( strcmp("moist", args) == 0 ) {
        result = io_command_read(sensor_moist, &value);
    } else if ( strcmp("bwf_l", args) == 0 ) {
        result = io_command_read(sensor_bwf_left, &value);
    } else if ( strcmp("bwf_r", args) == 0 ) {
        result = io_command_read(sensor_bwf_right, &value);
    } else if ( strcmp("bwf_ref", args) == 0 ) {
        result = io_command_read(sensor_bwf_reference, &value);
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

/**
 * @brief TODO
 *
 * @ingroup raspi_i2c
 *
 * @return      Success status
 */
static error_code wait_for_command()
{
    return err_OK;
}

/**
 * Send a message over I2C to I/O board.
 *
 * @ingroup raspi_i2c
 *
 * @param[in] msg       Message to send
 * @param[in] length    Length of message
 *
 * @return              Success status
 */
static error_code i2c_send_command(uint8_t* msg, int length)
{
    int i;
#ifdef SIMULATOR
#else
    int res;

    if ( length != MAX_MSG_SIZE ) {
        return err_WRONG_ARGUMENT;
    }

    res = i2c_smbus_write_block_data(this.fd, CMD_I2C_MAGIC, length, msg);
#endif // SIMULATOR

    if ( this.debug ) {
        printf("i2c:");
        i = 0;
        while ( i < length ) {
            printf(" %02x", msg[i]);
            i++;
        }
        printf("\n");
    }

    return err_OK;
}

/**
 * Read a message from I2C.
 *
 * @ingroup raspi_i2c
 *
 * @param[out] msg      Buffer to write to
 * @param[in]  length   Length of buffer
 *
 * @return              Success status
 */
static error_code i2c_read_data(uint8_t* msg, int length)
{
#ifdef SIMULATOR
    msg[0] = CMD_SEND;
    switch ( msg[1] ) {
        case CMD_RANGE_SENSOR:
            msg[3] = this.range >> 8;
            msg[4] = this.range & 0xff;
            break;
        case CMD_MOIST_SENSOR:
            msg[3] = this.moist >> 8;
            msg[4] = this.moist & 0xff;
            break;
        case CMD_VOLTAGE_SENSOR:
            msg[3] = this.voltage >> 8;
            msg[4] = this.voltage & 0xff;
            break;
        case CMD_BWF_LEFT_SENSOR:
            msg[3] = this.bwf_l >> 8;
            msg[4] = this.bwf_l & 0xff;
            break;
        case CMD_BWF_RIGHT_SENSOR:
            msg[3] = this.bwf_r >> 8;
            msg[4] = this.bwf_r & 0xff;
            break;
        case CMD_BWF_REFERENCE:
            msg[3] = this.bwf_ref >> 8;
            msg[4] = this.bwf_ref & 0xff;
            break;
        default:
            msg[3] = 0;
            msg[4] = 0;
            break;
    }
#else
    int i = 0;
    while ( i < length )
    {
        msg[i] = i2c_smbus_read_byte(this.fd);
        i++;
    }
#endif // SIMULATOR

    return err_OK;
}

#ifdef SIMULATOR
/**
 * Test command.
 *
 * @ingroup raspi_i2c
 *
 * @param[in] i     Value to send
 *
 * @return          Success status
 */
error_code io_test_command_1(int i)
{
    int res;
    uint8_t data[5] = { 11, 12, 13 ,14, i };
    int length = sizeof(data)/sizeof(data[0]);

//    res = i2c_smbus_write_byte(this.fd, i);
    res = i2c_smbus_write_block_data(this.fd, 6, length, data);

    printf("res1: %x\n", res);

    usleep(1000);

    return err_OK;
}

/**
 * Test command.
 *
 * @ingroup raspi_i2c
 *
 * @return          Success status
 */
error_code io_test_command_2()
{
    int i;
    int res;
    uint8_t data[I2C_SMBUS_BLOCK_MAX] = { 11, 12, 13 ,14, 15 };
    int length = sizeof(data)/sizeof(data[0]);

    res = i2c_smbus_write_byte(this.fd, 5);
    usleep(1000);
#if 1
    data[0] = i2c_smbus_read_byte(this.fd);
    data[1] = i2c_smbus_read_byte(this.fd);
    data[2] = i2c_smbus_read_byte(this.fd);
#else
    res = i2c_smbus_read_block_data(this.fd, 4, data);
#endif // 0

    if ( res < 0 ) {
        printf("failed to read, %x\n", res);
    }

    printf("res5: %x\n", res);
#if 1
    for ( i = 0; i < 3; i++ ) {
        printf("data[%d]: %x\n", i, data[i]);
    }
#endif // 0

    usleep(1000);

    return err_OK;
}
#endif // SIMULATOR

/**
 * Send a command to a motor.
 *
 * @ingroup raspi_i2c
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

    msg[0] = CMD_WRITE;

    switch ( direction ) {
        case direction_left:
            msg[1] = CMD_MOTOR_LEFT;
            break;
        case direction_right:
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
        i2c_send_command( msg, 4 );
    }

    return error;
}

/**
 * Send a command to a relay.
 *
 * @ingroup raspi_i2c
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

    switch ( direction ) {
        case direction_left:
            msg[1] = CMD_RELAY_LEFT;
            break;
        case direction_right:
            msg[1] = CMD_RELAY_RIGHT;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    switch ( command ) {
        case direction_forward:
            msg[2] = 1;
            break;
        case direction_backward:
            msg[2] = 0;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) ) {
        i2c_send_command( msg, 4 );
    }

    return error;
}

/**
 * Read a value from the I/O board.
 *
 * @ingroup raspi_i2c
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

    switch ( sensor ) {
        case sensor_range:
            msg[1] = CMD_RANGE_SENSOR;
            break;
        case sensor_moist:
            msg[1] = CMD_MOIST_SENSOR;
            break;
        case sensor_voltage:
            msg[1] = CMD_VOLTAGE_SENSOR;
            break;
        case sensor_bwf_left:
            msg[1] = CMD_BWF_LEFT_SENSOR;
            break;
        case sensor_bwf_right:
            msg[1] = CMD_BWF_RIGHT_SENSOR;
            break;
        case sensor_bwf_reference:
            msg[1] = CMD_BWF_REFERENCE;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) ) {
        i2c_send_command( msg, 4 );
        usleep(1000);
        i2c_read_data( msg, 4 );
        *value = (msg[3] << 8) + msg[4];
    }

    return error;

 
}
