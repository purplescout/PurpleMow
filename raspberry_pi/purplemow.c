
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "raspi_io.h"
#include "auto_management.h"
#include "dcn.h"
#include "messages.h"
#include "utils.h"
#include "purplemow.h"

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

/**
 * @defgroup purplemow PurpleMow
 * PurpleMow.
 */

/**
 * @defgroup purplemow_main PurpleMow
 * Main thread with the main FSM.
 *
 * @ingroup purplemow
 */

// cli commands
static error_code command_main(char *args);

// Private
static error_code main_init();
static void process_events();
static error_code handle_sensor(enum sensor sensor, enum decision decision);

/**
 * purplemow
 *
 * @ingroup purplemow_main
 */
struct purplemow {
    struct message_queue    message_handler;
    int                     debug;
};

static struct purplemow this;

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

    this.debug = 0;

    /*************
     *  I N I T  *
     *************/

    if ( this.debug )
        printf("Initializing... ");

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

#if DO_TEST
    test_init();
#endif // DO_TEST

#if DO_TEST_THREADS
    test_thread_init();
#endif // DO_TEST_THREADS

    main_init();

    if ( this.debug )
        printf("OK\n");

    /**************
     *  S T A R T *
     **************/

    if ( this.debug )
        printf("Starting... ");

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
    sensor_range_start();
#endif // DO_SEN_RANGE

#if DO_DCN
    dcn_start();
#endif // DO_DCN

#if DO_NET
    multicast_start();
#endif // DO_NET

    if ( this.debug )
        printf("OK\n");

    process_events();
}

/**
 * Initialize main.
 *
 * @ingroup purplemow_main
 *
 * @return          Success status
 */
static error_code main_init()
{
    error_code result;

    cli_register_command("main", command_main);

    result = message_open(&this.message_handler, Q_MAIN);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}

/**
 * Main FSM in purplemowi.
 * Handles incoming messages and takes decisions depending on them,
 *
 * @ingroup purplemow_main
 */
static void process_events()
{
    error_code result;
    struct message_item msg_buff;
    int len;

    while ( 1 )
    {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this.message_handler, &msg_buff, &len);

        if ( SUCCESS(result) ) {
            switch ( msg_buff.head.type ) {
                case MSG_SENSOR_DECISION:
                    {
                        struct message_sensor_decision *msg;
                        msg = (struct message_sensor_decision*)&msg_buff;
                        handle_sensor(msg->body.sensor,
                                      msg->body.decision);
                    break;
                    }
            }
        }
    }
}

/**
 * The command <b>main</b>, debugging options for main.
 *
 * @ingroup purplemow
 *
 * @param[in] args  Arguments
 *
 * @return          Success status
 */
static error_code command_main(char *args)
{
    if ( strcmp("debug", args) == 0 ) {
        printf("Enabled main debugging\n");
        this.debug = 1;
    } else if ( strcmp("nodebug", args) == 0 ) {
        printf("Disabled main debugging\n");
        this.debug = 0;
    } else {
        printf("Valid arguments: debug, nodebug"
                "\n");
    }
    return err_OK;
}

/**
 * Send a message to main that something is too close to the range sensor.
 *
 * @ingroup purplemow
 *
 * @return      Success status
 */
error_code main_range_too_close()
{
    struct message_sensor_decision msg;

    message_create(msg, struct message_sensor_decision, MSG_SENSOR_DECISION);

    msg.body.sensor = sensor_range;
    msg.body.decision = decision_range_too_close;

    message_send(&msg, Q_MAIN);

    return err_OK;
}

/**
 * Send a message to main the range sensor is cleared.
 *
 * @ingroup purplemow
 *
 * @return      Success status
 */
error_code main_range_ok()
{
    struct message_sensor_decision msg;

    message_create(msg, struct message_sensor_decision, MSG_SENSOR_DECISION);

    msg.body.sensor = sensor_range;
    msg.body.decision = decision_range_ok;

    message_send(&msg, Q_MAIN);

    return err_OK;
}

/**
 * Handle a decision from a sensor.
 *
 * @ingroup purplemow
 *
 * @param[in] sensor        Sensor
 * @param[in] decision      Decision
 *
 * @return                  Success status
 */
static error_code handle_sensor(enum sensor sensor, enum decision decision)
{
    switch ( sensor ) {
        case sensor_range:
            switch ( decision ) {
                case decision_range_too_close:
                    if ( this.debug )
                        printf("Main: Range too close, moving backwards\n");
                    communicator_move_backward();
                    break;
                case decision_range_ok:
                    {
                        int random = 0;
                        get_random(&random);

                        if ( random & 1 ) {
                            if ( this.debug )
                                printf("Main: Range OK, turn left\n");
                            communicator_turn_left();
                        } else {
                            if ( this.debug )
                                printf("Main: Range OK, turn right\n");
                            communicator_turn_right();
                        }
                        if ( this.debug )
                            printf("Main: Range OK, moving forward\n");
                        communicator_move_forward();
                    break;
                    }
            }
            break;
    }

    return err_OK;
}

