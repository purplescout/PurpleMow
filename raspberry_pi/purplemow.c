
#include <stdio.h>
#include <stdlib.h>

#include "raspi_io.h"
#include "auto_management.h"
#include "dcn.h"

#include "test_thread.h"

#define DO_ARGS     0
#define DO_I2C      1
#define DO_COMM     1
#define DO_NET      0
#define DO_TEST_THREADS  0
#define DO_SEN_RANGE 1

int main(int argc, char **argv)
{
    int state;

#if DO_ARGS
    // args on the command line
    if ( argc < 2 )
    {
        printf("Wrong\n");
        exit(0);
    }
#endif // DO_ARGS

    /*************
     *  I N I T  *
     *************/

    cli_init();

#if DO_I2C
    // i2c stuff
    purple_io_init();
#endif // DO_I2C

#if DO_COMM
    communicator_init();
#endif // DO_COMM

#if DO_SEN_RANGE
    sensor_range_init();
#endif // DO_SEN_RANGE

#if DO_NET
    dcn_init();
    multicast_init();
#endif // DO_NET

#if DO_TEST_THREADS
    test_thread_init();
#endif // DO_TEST_THREADS

    /**************
     *  S T A R T *
     **************/

    cli_start();

#if DO_COMM
    communicator_start();
#endif // DO_COMM


#if DO_TEST_THREADS
    test_thread_start();
#endif // DO_TEST_THREADS

#if DO_SEN_RANGE
    sensor_range_start();
#endif // DO_SEN_RANGE

#if DO_NET
    dcn_start();
    multicast_start();
#endif // DO_NET

    while ( 1 )
    {
        sleep(30);
    }
}
