
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "messages.h"
#include "modules.h"
#include "purplemow.h"


/**
 * @defgroup purplemow Main FSM
 * Main thread with the main FSM.
 */

// cli commands
static error_code command_main(char *args);

// Private
static error_code purplemow_mow();
static error_code handle_sensor(enum sensor sensor, enum decision decision);

/**
 * purplemow
 *
 * @ingroup purplemow
 */
struct purplemow {
    struct message_queue    message_handler;
    int                     debug;
};

static struct purplemow this;

/**
 * Initialize purplemow.
 *
 * @ingroup purplemow
 *
 * @return          Success status
 */
error_code purplemow_init()
{
    error_code result;

    cli_register_command("main", command_main);

    result = message_open(&this.message_handler, Q_MAIN);

    if ( FAILURE(result) )
        return result;

    module_register_to_phase(phase_MOW, purplemow_mow);

    return err_OK;
}

/**
 * Main FSM in purplemow.
 * Handles incoming messages and takes decisions depending on them,
 *
 * @ingroup purplemow
 */
static error_code purplemow_mow()
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

