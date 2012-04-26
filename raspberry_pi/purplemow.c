
#include <stdio.h>
#include <stdlib.h>

#include "raspi_i2c.h"
#include "auto_management.h"
#include "dcn.h"

#include "test_thread.h"

int main(int argc, char **argv)
{
    int state;

#if 0
    // args on the command line
    if ( argc < 2 )
    {
        printf("Wrong\n");
        exit(0);
    }
#endif // 0

    cli_init();

#if 1
    // i2c stuff
    purple_io_init();
#endif // 0

#if 1
    test_thread_init();
#endif // 0

    cli_start();

#if 1
    test_thread_start();
#endif // 0


#if 0
    dcn_init();

    multicast_init();
    multicast_start();
#endif // 0

    while ( 1 )
    {
        sleep(30);
    }
}
