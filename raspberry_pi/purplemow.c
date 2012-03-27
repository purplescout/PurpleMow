
#include <stdio.h>
#include <stdlib.h>

#include "raspi_i2c.h"
#include "auto_management.h"
#include "dcn.h"

int main(int argc, char **argv)
{
    int state;

    if ( argc < 2 )
    {
        printf("Wrong\n");
        exit(0);
    }

    state = atoi(argv[1]);

#if 0
    // i2c stuff
    purple_io_init();

    printf("Setting state: %d\n", state);
    io_test_command_1(state);
    io_test_command_2();
#endif // 0

    cli_init();
    cli_start();

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
