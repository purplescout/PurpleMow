
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
#define DO_THREADS  0

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

    cli_init();

#if DO_I2C
    // i2c stuff
    purple_io_init();
#endif // DO_I2C

#if DO_COMM
    // communicator
    communicator_init();
#endif // DO_COMM

#if DO_THREADS
    test_thread_init();
#endif // DO_THREADS

    cli_start();

#if DO_THREADS
    test_thread_start();
#endif // DO_THREADS


#if DO_NET
    dcn_init();

    multicast_init();
    multicast_start();
#endif // DO_NET

    while ( 1 )
    {
        sleep(30);
    }
}
