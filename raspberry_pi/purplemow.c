
#include "raspi_i2c.h"
#include "auto_management.h"
#include "dcn.h"

int main(int argc, char **argv)
{

    purple_io_init();

    dcn_init();

    multicast_init();
    multicast_start();

    while ( 1 )
    {
        sleep(30);
    }
}
