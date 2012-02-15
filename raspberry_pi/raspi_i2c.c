
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include "include/linux/i2c-dev.h"

#include <stdlib.h>  // exit

#include "raspi_i2c.h"

#define I2C_DEVICE "/dev/i2c-1"
//#define I2C_DEVICE "/dev/i2c-4"
#define I2C_ADDRESS 0x50

static int test_command(void);

int i2c_fd = -1;

int purple_io_init()
{
    int res;

    i2c_fd = open(I2C_DEVICE, O_RDWR);

    if ( i2c_fd < 0 )
    {
        perror("opening i2c device");
        return 1;
    }

    res = ioctl(i2c_fd, I2C_SLAVE, I2C_ADDRESS);

    if ( res < 0 )
    {
        perror("setting i2c device to slave");
        return 2;
    }

    test_command();

    return 0;
}

static int test_command()
{
    int res;
    unsigned char block[256];
    int i;

    i = 0;
    while ( i < 256 )
    {
        block[i] = res = i2c_smbus_read_byte_data(i2c_fd, i);

        if ( res >= 'a' && res <= 'z' ||
             res >= 'A' && res <= 'Z' ||
             res >= '0' && res <= '9' )
            printf("res: %x %c\n", res, res);

        i++;
    }

    exit(0);

    return 0;
}
