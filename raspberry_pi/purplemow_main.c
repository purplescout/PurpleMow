
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "raspi_io.h"
#include "auto_management.h"
#include "dcn.h"
#include "messages.h"
#include "utils.h"
#include "purplemow.h"
#include "purplemow_main.h"
#include "sensors.h"

#include "test.h"
#include "test_thread.h"

#define DO_ARGS     0
#define DO_I2C      1
#define DO_COMM     1
#define DO_DCN      1
#define DO_NET      0
#define DO_SEN_RANGE 1

#define DO_TEST          0
#define DO_TEST_THREADS  0

// Set this to 1 to enable debug of main()
#define DEBUG_MAIN  0

/**
 * @defgroup purplemow_main PurpleMow Main
 * PurpleMow main function.
 */

/**
 * purplemow_main
 *
 * @ingroup purplemow_main
 */
struct purplemow {
    int                     debug;
};

static struct purplemow this = { .debug = DEBUG_MAIN };

/**
 * Main function, initializes and starts all the modules.
 *
 * @ingroup purplemow_main
 *
 * @param[in] argc      Agrument count
 * @param[in] argv      Argument vector
 *
 * @return              Success status
 */
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

    if ( this.debug )
        printf("Initializing... ");

    message_init();
    cli_init();

#if DO_I2C
    // i2c stuff
    purple_io_init();
#endif // DO_I2C

#if DO_COMM
    communicator_init();
#endif // DO_COMM

#if DO_SEN_RANGE
    sensors_init();
#endif // DO_SEN_RANGE

#if DO_DCN
    dcn_init();
#endif // DO_DCN

#if DO_NET
    multicast_init();
#endif // DO_NET

#if DO_TEST
    test_init();
#endif // DO_TEST

#if DO_TEST_THREADS
    test_thread_init();
#endif // DO_TEST_THREADS

    purplemow_init();

    if ( this.debug )
        printf("OK\n");

    /**************
     *  S T A R T *
     **************/

    if ( this.debug )
        printf("Starting... ");

    message_start();
    cli_start();

#if DO_COMM
    communicator_start();
#endif // DO_COMM

#if DO_TEST
    test_start();
#endif // DO_TEST

#if DO_TEST_THREADS
    test_thread_start();
#endif // DO_TEST_THREADS

#if DO_SEN_RANGE
    sensors_start();
#endif // DO_SEN_RANGE

#if DO_DCN
    dcn_start();
#endif // DO_DCN

#if DO_NET
    multicast_start();
#endif // DO_NET

    if ( this.debug )
        printf("OK\n");

    purplemow_start();
}

