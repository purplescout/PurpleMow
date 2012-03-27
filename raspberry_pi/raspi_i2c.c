
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdint.h>
#include "include/linux/i2c-dev.h"

#include <stdlib.h>  // exit

#include "raspi_i2c.h"

#define I2C_DEVICE "/dev/i2c-2"
//#define I2C_DEVICE "/dev/i2c-4"
#define I2C_ADDRESS 0x35

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

    return 0;
}

int io_test_command_1(int i)
{
    int res;
    uint8_t data[5] = { 11, 12, 13 ,14, i };
    int length = sizeof(data)/sizeof(data[0]);

//    res = i2c_smbus_write_byte(i2c_fd, i);
    res = i2c_smbus_write_block_data(i2c_fd, 6, length, data);

    printf("res1: %x\n", res);

    usleep(1000);

    return 0;
}

int io_test_command_2()
{
    int i;
    int res;
    uint8_t data[I2C_SMBUS_BLOCK_MAX] = { 11, 12, 13 ,14, 15 };
    int length = sizeof(data)/sizeof(data[0]);

    res = i2c_smbus_write_byte(i2c_fd, 5);
    usleep(1000);
#if 1
    data[0] = i2c_smbus_read_byte(i2c_fd);
    data[1] = i2c_smbus_read_byte(i2c_fd);
    data[2] = i2c_smbus_read_byte(i2c_fd);
#else
    res = i2c_smbus_read_block_data(i2c_fd, 4, data);
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

    return 0;
}
