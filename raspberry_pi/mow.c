
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "messages.h"
#include "modules.h"
#include "mow.h"
#include "utils/state.h"


/**
 * @defgroup mow Mow FSM
 * Main thread with the mow FSM.
 */

// cli commands
static error_code command_mow(char *args);

// Private
static error_code mow_mow();
static error_code mow_state_debug(int debug);

// Functions for states
static error_code handle_sensor_range(struct message_item* message);
static error_code handle_sensor_bwf(struct message_item* message);

struct state_table state_table_stopped[] = {
    { MSG_SENSOR_RANGE,         handle_sensor_range         },
    { MSG_SENSOR_BWF,           handle_sensor_bwf           },
};

struct state_table state_table_idle[] = {
    { MSG_SENSOR_RANGE,         handle_sensor_range         },
    { MSG_SENSOR_BWF,           handle_sensor_bwf           },
};

struct state_table state_table_obstacle[] = {
    { MSG_SENSOR_RANGE,         handle_sensor_range         },
    { MSG_SENSOR_BWF,           handle_sensor_bwf           },
    { MSG_ANY,                  state_stash                 },
};

enum mow_states {
    state_stopped,
    state_idle,
    state_obstacle,
    state_last
};

/**
 * mow
 *
 * @ingroup mow
 */
struct mow {
    struct message_queue    message_handler;
    int                     debug;
    state_t                 current_state;
    state_t                 states[state_last];
};

static struct mow this;

/**
 * Initialize mow.
 *
 * @ingroup mow
 *
 * @return          Success status
 */
error_code mow_init()
{
    error_code result;

    cli_register_command("mow", command_mow);

    result = message_open(&this.message_handler, Q_MAIN);

    if ( FAILURE(result) )
        return result;

    module_register_to_phase(phase_MOW, mow_mow);

    state_create(&this.states[state_stopped],
                 state_table_stopped,
                 STATE_TABLE_SIZE(state_table_stopped),
                 "mow stopped");

    state_create(&this.states[state_idle],
                 state_table_idle,
                 STATE_TABLE_SIZE(state_table_idle),
                 "mow idle");

    state_create(&this.states[state_obstacle],
                 state_table_obstacle,
                 STATE_TABLE_SIZE(state_table_obstacle),
                 "mow obstacle");

    state_change(&this.current_state, this.states[state_stopped]);

    return err_OK;
}

/**
 * Main FSM in mow.
 * Handles incoming messages and takes decisions depending on them,
 *
 * @ingroup mow
 */
static error_code mow_mow()
{
    error_code result;

    while ( 1 )
    {
        result = state_next(&this.message_handler, this.current_state);
    }
}

/**
 * The command <b>mow</b>, debugging options for mow.
 *
 * @ingroup mow
 *
 * @param[in] args  Arguments
 *
 * @return          Success status
 */
static error_code command_mow(char *args)
{
    if ( strcmp("debug", args) == 0 ) {
        printf("Enabled mow debugging\n");
        this.debug = 1;
        mow_state_debug(1);
    } else if ( strcmp("nodebug", args) == 0 ) {
        printf("Disabled mow debugging\n");
        this.debug = 0;
        mow_state_debug(0);
    } else if ( strcmp("debugstate", args) == 0 ) {
        mow_state_debug(1);
    } else if ( strcmp("nodebugstate", args) == 0 ) {
        mow_state_debug(0);
    } else {
        printf("Valid arguments: debug, nodebug, debugstate, nodebugstate"
                "\n");
    }
    return err_OK;
}

static error_code mow_state_debug(int debug)
{
    int i = 0;
    while ( i < state_last ) {
        state_debug(this.states[i], debug);
        i++;
    }

    return err_OK;
}

/**
 * Send a message to mow that something happened on a range sensor.
 *
 * @ingroup mow
 *
 * @param[in] sensor        Sensor
 * @param[in] decision      Decision
 *
 * @return      Success status
 */
error_code mow_range(enum sensor sensor, enum decision decision)
{
    struct message_sensor_decision msg;

    message_create(msg, struct message_sensor_decision, MSG_SENSOR_RANGE);

    msg.body.sensor = sensor;
    msg.body.decision = decision;

    message_send(&msg, Q_MAIN);

    return err_OK;
}

static error_code handle_sensor_range(struct message_item* message)
{
    struct message_sensor_decision *msg;
    msg = (struct message_sensor_decision*)message;

    switch ( msg->body.decision )
    {
        case decision_range_too_close:
            if ( this.debug )
                printf("Mow: Range too close, moving backwards\n");
            communicator_move_backward();

            state_change(&this.current_state, this.states[state_obstacle]);

            break;
        case decision_range_ok:
            {
                int random = 0;
                get_random(&random);

                if ( random & 1 ) {
                    if ( this.debug )
                        printf("Mow: Range OK, turn left\n");
                    communicator_turn_left();
                } else {
                    if ( this.debug )
                        printf("Mow: Range OK, turn right\n");
                    communicator_turn_right();
                }
                if ( this.debug )
                    printf("Mow: Range OK, moving forward\n");
                communicator_move_forward();

                state_change(&this.current_state, this.states[state_idle]);

                break;
            }
    }

    return err_OK;
}

static error_code handle_sensor_bwf(struct message_item* message)
{
    if ( state_equal(this.current_state, this.states[state_obstacle]) == err_EQUAL )
        state_change(&this.current_state, this.states[state_idle]);

    // TODO: 1. need to know which direction the mower is moving in
    //       2. reverse direction until bwf is cleared
    //       3. turn x degrese in random direction
    //       4. continue in original direction

    return err_NOT_IMPLEMENTED;
}

