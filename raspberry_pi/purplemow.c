
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "raspi_io.h"
#include "auto_management.h"
#include "dcn.h"
#include "messages.h"

#include "test_thread.h"

#define DO_ARGS     0
#define DO_I2C      1
#define DO_COMM     1
#define DO_DCN      1
#define DO_NET      0
#define DO_TEST_THREADS  0
#define DO_SEN_RANGE 1

static void process_events();

struct purplemow {
    struct message_queue    message_handler;
};

static struct purplemow this;

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

#if DO_DCN
    dcn_init();
#endif // DO_DCN

#if DO_NET
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

#if DO_DCN
    dcn_start();
#endif // DO_DCN

#if DO_NET
    multicast_start();
#endif // DO_NET

    process_events();
}

static void process_events()
{
    error_code result;
    struct message_item msg_buff;
    int len;

    result = message_open(&this.message_handler, Q_MAIN);

    while ( 1 )
    {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this.message_handler, &msg_buff, &len);

        if ( SUCCESS(result) ) {
        }
    }
}
