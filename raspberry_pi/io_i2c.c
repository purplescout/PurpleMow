
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdint.h>
#include "include/linux/i2c-dev.h"

#include <stdlib.h>  // exit

#include "modules.h"

#include "io.h"
#include "commands.h"

#define I2C_DEVICE "/dev/i2c-0"
//#define I2C_DEVICE "/dev/i2c-4"
#define I2C_ADDRESS 0x35

/**
 * @defgroup io_i2c Raspberry PI I2C IO
 * I2C implementation for the IO Interface.
 *
 * @ingroup io
 */

// cli commands
static error_code command_i2c(char *args, int (*print)(const char *format, ...));

// i2c
static error_code i2c_send_command(uint8_t* msg, int length);
static error_code i2c_read_data(uint8_t* msg, int length);

/**
 * i2c
 *
 * @ingroup io_i2c
 */
struct i2c
{
    int fd;
    struct timeval last_command;
    int debug_rx;
    int debug_tx;
};

#if 1 // NODEBUG
static struct i2c this = { .fd = -1, .debug_rx = 0, .debug_tx = 0 };
#else // DEBUG
static struct i2c this = { .fd = -1, .debug_rx = 1, .debug_tx = 1 };
#endif

// Private functions
static error_code io_i2c_start();
static error_code io_i2c_stop();

/**
 * Initialize the i2c.
 *
 * @ingroup io_i2c
 *
 * @return          Success status
 */
error_code io_transport_init()
{
    module_register_to_phase(phase_START, io_i2c_start, NULL);
    module_register_to_phase(phase_STOP, io_i2c_stop, NULL);
    return err_OK;
}

static error_code io_i2c_start()
{
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

    cli_register_command("i2c", command_i2c);

    return err_OK;
}

static error_code io_i2c_stop()
{
    if ( this.fd > 0 ) {
        close(this.fd);
    }

    return err_OK;
}

/**
 * The command <b>i2c</b>, debugging options for I2C.
 * Modify readable values in simulator mode.
 *
 * @ingroup io_i2c
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static error_code command_i2c(char *args, int (*print)(const char *format, ...))
{
    if ( strcmp("debug", args) == 0 ) {
        print("Enabled i2c debugging\n");
        this.debug_rx = 1;
        this.debug_tx = 1;
    } else if ( strcmp("debug rx", args) == 0 ) {
        print("Enabled i2c (RX) debugging\n");
        this.debug_rx = 1;
    } else if ( strcmp("debug tx", args) == 0 ) {
        print("Enabled i2c (TX) debugging\n");
        this.debug_tx = 1;
    } else if ( strcmp("nodebug", args) == 0 ) {
        print("Disabled i2c debugging\n");
        this.debug_rx = 0;
        this.debug_tx = 0;
    } else if ( strcmp("nodebug rx", args) == 0 ) {
        print("Disabled i2c (RX) debugging\n");
        this.debug_rx = 0;
    } else if ( strcmp("nodebug tx", args) == 0 ) {
        print("Disabled i2c (TX) debugging\n");
        this.debug_tx = 0;
    } else {
        print("Valid arguments: debug, nodebug, debug rx, debug tx, nodebug rx nodebug tx"
              "\n");
    }
    return err_OK;
}


/**
 * @brief TODO
 *
 * @ingroup io_i2c
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
 * @ingroup io_i2c
 *
 * @param[in] msg       Message to send
 * @param[in] length    Length of message
 *
 * @return              Success status
 */
error_code io_transport_send_command(uint8_t* msg, int length)
{
    int i;
    int res;

    if ( length != MAX_MSG_SIZE ) {
        return err_WRONG_ARGUMENT;
    }

    res = i2c_smbus_write_block_data(this.fd, CMD_I2C_MAGIC, length, msg);

    if ( this.debug_tx ) {
        printf("i2c (TX):");
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
 * @ingroup io_i2c
 *
 * @param[out] msg      Buffer to write to
 * @param[in]  length   Length of buffer
 *
 * @return              Success status
 */
error_code io_transport_read_data(uint8_t* msg, int length)
{
    int i = 0;
    while ( i < length )
    {
        msg[i] = i2c_smbus_read_byte(this.fd);
        i++;
    }

    length = i - 1;

    if ( this.debug_rx ) {
        printf("i2c (RX):");
        i = 0;
        while ( i < length ) {
            printf(" %02x", msg[i]);
            i++;
        }
        printf("\n");
    }

    return err_OK;
}

