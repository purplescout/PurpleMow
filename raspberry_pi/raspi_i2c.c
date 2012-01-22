
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <i2c-dev.h>

#include "raspi_i2c.h"

#define I2C_DEVICE "/dev/i2c-0"
#define I2C_ADDRESS 0x50

static int test_command(void);

int i2c_fd = -1;

int purple_io_init()
{
    int res;

    i2c_fd = open(I2C_DEVICE, O_RDWR);

    if ( fd < 0 )
    {
        return 1;
    }

    res = ioctl(i2c_fd, I2C_SLAVE, I2C_ADDRESS);

    if ( res < 0 )
    {
        return 2;
    }

    test_command();

    return 0;
}

static int test_command()
{
    int res;

//    res = i2c_smbus_read_byte_data(i2c_fd, );

}
