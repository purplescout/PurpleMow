
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdint.h>
#include "include/linux/i2c-dev.h"

#include <stdlib.h>  // exit

#include "raspi_io.h"
#include "../arduino/PurpleMow/commands.h"

#define I2C_DEVICE "/dev/i2c-2"
//#define I2C_DEVICE "/dev/i2c-4"
#define I2C_ADDRESS 0x35

static error_code command_i2c(char *args);

struct i2c
{
    int fd;
    struct timeval last_command;
    int debug;
};

static struct i2c this = { .fd = -1, .debug = 0 };

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

    return err_OK;
}

static error_code command_i2c(char *args)
{
    if ( strcmp("debug", args) == 0 ) {
        printf("Enabled i2c debugging\n");
        this.debug = 1;
    } else if ( strcmp("nodebug", args) == 0 )
    {
        printf("Disabled i2c debugging\n");
        this.debug = 0;
    } else {
        printf("Valid arguments: debug, nodebug\n");
    }
    return err_OK;
}

static error_code wait_for_command()
{
    return err_OK;
}

static error_code i2c_send_command(uint8_t* msg, int length)
{
    int i;
#ifdef SIMULATOR
#else
    int res;

    if ( length != MAX_MSG_SIZE )
    {
        return err_WRONG_ARGUMENT;
    }

    res = i2c_smbus_write_block_data(this.fd, CMD_I2C_MAGIC, length, msg);
#endif // SIMULATOR

    if ( this.debug )
    {
        printf("i2c:");
        i = 0;
        while ( i < length )
        {
            printf(" %02x", msg[i]);
            i++;
        }
        printf("\n");
    }

    return err_OK;
}

#ifdef SIMULATOR
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

    if ( res < 0 )
    {
        printf("failed to read, %x\n", res);
    }

    printf("res5: %x\n", res);
#if 1
    for ( i = 0; i < 3; i++ )
    {
        printf("data[%d]: %x\n", i, data[i]);
    }
#endif // 0

    usleep(1000);

    return err_OK;
}
#endif // SIMULATOR

error_code command_motor(enum direction direction, enum command command, int speed)
{
    int error = err_OK;
    uint8_t msg[4] = { 0 };

    msg[0] = CMD_WRITE;

    switch ( direction )
    {
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

    switch ( command )
    {
        case command_start:
            msg[2] = speed;
            break;
        case command_stop:
            msg[2] = speed;
            break;
        default:
            error = err_WRONG_ARGUMENT;
            break;
    }

    if ( SUCCESS(error) )
    {
        i2c_send_command( msg, 4 );
    }

    return error;
}

error_code command_relay(enum direction direction, enum direction command)
{
    int error = err_OK;
    uint8_t msg[4] = { 0 };

    msg[0] = CMD_RELAY;

    switch ( direction )
    {
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

    switch ( command )
    {
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

    if ( SUCCESS(error) )
    {
        i2c_send_command( msg, 4 );
    }

    return error;
}
