
#include <stdio.h>
#include <string.h>

#include "thread.h"
#include "messages.h"
#include "communicator.h"
#include "io.h"
#include "modules.h"

#define DELAY 1

/**
 * @defgroup communicator Communicator
 * Communicator. All messages to hardware should go through this module.
 *
 * @ingroup purplemow
 */

/**
 * Communicator
 *
 * @ingroup communicator
 */
struct communicator {
    enum                    direction direction;
    enum                    command motor;
    int                     speed;
    struct message_queue    message_handle;
    pthread_t               thread;
    int                     debug;
};

static struct communicator this = { 0 };

static void* communicator_worker(void *data);

static void move(enum direction direction);
static void turn(enum direction direction);

// cli commands
static int command_move(char *args, int (*print)(const char *format, ...));

static error_code move_forward();
static error_code move_backward();
static error_code turn_left();
static error_code turn_right();
static error_code set_speed(int speed);
static error_code stop();
static error_code sensor(enum sensor sensor, enum queue rsp_queue);

static error_code communicator_start(void* data);

/**
 * Initialize the communicator.
 *
 * @ingroup communicator
 *
 * @return          Success status
 */
error_code communicator_init()
{
    this.direction = direction_undefined;
    this.motor = command_stop;
    this.speed = 255;
    this.debug = 0;

    cli_register_command("move", command_move);

    message_open(&this.message_handle, Q_COMMUNICATOR);

    module_register_to_phase(phase_START, communicator_start, NULL);

    return err_OK;
}

/**
 * Start the communicator.
 *
 * @ingroup communicator
 *
 * @return          Success status
 */
static error_code communicator_start(void* data)
{
    error_code result;

    result = thread_start(&this.thread, communicator_worker);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

/**
 * Handles incoming messages.
 *
 * @ingroup communicator
 *
 * @parami[in] data     Data ti the thread
 *
 * @return              Return value from thread
 */
static void* communicator_worker(void *data)
{
    struct message_item         msg_buff;
    struct message_communicator *msg;
    int len;
    error_code result;

    while ( 1 ) {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this.message_handle, &msg_buff, &len);

        if ( SUCCESS(result) )
        {
            msg = (struct message_communicator*)&msg_buff;
            switch ( msg->body.message ) {
                case msg_communicator_forward:
                    move_forward();
                    break;
                case msg_communicator_backward:
                    move_backward();
                    break;
                case msg_communicator_left:
                    turn_left();
                    break;
                case msg_communicator_right:
                    turn_right();
                    break;
                case msg_communicator_speed:
                    set_speed(msg->body.data);
                    break;
                case msg_communicator_stop:
                    stop();
                    break;
                case msg_communicator_sensor:
                    sensor(msg->body.sensor, msg->body.queue);
                    break;
                default:
                    break;
            }
        }
    }
}

/**
 * Send a read request to communicator on specified sensor.
 *
 * @ingroup communicator
 *
 * @param[in] sensor        Sensor to read
 * @param[in] rsp_queue     Queue to send the response to
 *
 * @return                  Success status
 */
error_code communicator_read(enum sensor sensor, enum queue rsp_queue)
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_sensor;
    msg.body.sensor = sensor;
    msg.body.queue = rsp_queue;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a set speed request to communicator, used for the motor speed.
 *
 * @ingroup communicator
 *
 * @param[in] speed     Speed to set
 *
 * @return              Success status
 */
error_code communicator_set_speed(int speed)
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_speed;
    msg.body.data = speed;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a request to stop the motors to communicator.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
error_code communicator_stop()
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_stop;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a request to move forward to the communicator.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
error_code communicator_move_forward()
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_forward;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a request to move backward to the communicator.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
error_code communicator_move_backward()
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_backward;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a request to turn left to the communicator.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
error_code communicator_turn_left()
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_left;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * Send a request to turn right to the communicator.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
error_code communicator_turn_right()
{
    struct message_communicator msg;
    message_create(msg, struct message_communicator, MSG_COMMUNICATOR);

    msg.body.message = msg_communicator_right;

    message_send(&msg, Q_COMMUNICATOR);

    return err_OK;
}

/**
 * The command <b>move</b>, control the movement.
 *
 * @ingroup communicator
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_move(char *args, int (*print)(const char *format, ...))
{
    if ( strcmp("forward", args) == 0 ) {
        communicator_move_forward();
    } else if ( strcmp("backward", args) == 0 ) {
        communicator_move_backward();
    } else if ( strcmp("left", args) == 0 ) {
        communicator_turn_left();
    } else if ( strcmp("right", args) == 0 ) {
        communicator_turn_right();
    } else if ( strcmp("stop", args) == 0 ) {
        communicator_stop();
    } else if ( strncmp("speed", args, strlen("speed")) == 0 ) {
        int speed;
        speed = cli_read_int(args);
        communicator_set_speed(speed);
    } else if ( strcmp("debug", args) == 0 ) {
        print("Enabled communicator debugging\n");
        this.debug = 1;
    } else if ( strcmp("nodebug", args) == 0 ) {
        print("Disabled communicator debugging\n");
        this.debug = 0;
    } else {
        print("Valid arguments: forward, backward, left, right, stop, speed [0-255]\n");
    }
    return 0;
}

/**
 * Move forward.
 *
 * @ingroup communicator
 *
 * @return          Success status
 */
static error_code move_forward()
{
    if ( this.debug )
        printf("Comm: Moving forward\n");
    move(direction_forward);
    return err_OK;
}

/**
 * Move backward.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
static error_code move_backward()
{
    if ( this.debug )
        printf("Comm: Moving backward\n");
    move(direction_backward);
    return err_OK;
}

/**
 * Turn left.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
static error_code turn_left()
{
    if ( this.debug )
        printf("Comm: Turning left\n");
    turn(direction_left);
    return err_OK;
}

/**
 * Turn right.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
static error_code turn_right()
{
    if ( this.debug )
        printf("Comm: Turning right\n");
    turn(direction_right);
    return err_OK;
}

/**
 * Set speed.
 *
 * @ingroup communicator
 *
 * @param[in] speed     Speed to set
 *
 * @return              Success status
 */
static error_code set_speed(int speed)
{
    this.speed = speed > 255 ? 255 : speed;
    return err_OK;
}

/**
 * Send commands to IO.
 *
 * @ingroup communicator
 *
 * @param[in] direction     Direction to update to
 */
static void move(enum direction direction)
{
    if ( direction != this.direction )
    {
        // Stop motors
        io_command_motor(direction_left, command_stop, 0);
        io_command_motor(direction_right, command_stop, 0);

        sleep(DELAY);

        // Change direction on relays
        io_command_relay(direction_left, direction);
        io_command_relay(direction_right, direction);

        sleep(DELAY);

        this.direction = direction;
    }

    // Move forward
    io_command_motor(direction_left, command_start, this.speed);
    io_command_motor(direction_right, command_start, this.speed);

    this.motor = command_start;
}

/**
 * Read a sensor and send response to response eueue
 *
 * @ingroup communicator
 *
 * @param[in] sensor        Sensor to read
 * @param[in] rsp_queue     Queue to send response to
 *
 * @return                  Success status
 */
static error_code sensor(enum sensor sensor, enum queue rsp_queue)
{
    int value = 0;
    struct message_sensor_data msg;

    io_command_read(sensor, &value);

    message_create(msg, struct message_sensor_data, MSG_SENSOR_DATA);

    msg.body.sensor = sensor;
    msg.body.value = value;

    message_send(&msg, rsp_queue);

    return err_OK;
}

/**
 * Send commands to IO to stop all motors.
 *
 * @ingroup communicator
 *
 * @return              Success status
 */
static error_code stop()
{
    io_command_motor(direction_left, command_stop, 0);
    io_command_motor(direction_right, command_stop, 0);

    this.motor = command_stop;
    return err_OK;
}

/**
 * Send commands to make a turn.
 *
 * @ingroup communicator
 *
 * @param[in] direction     Direction to turn
 */
static void turn(enum direction direction)
{
    // Stop motors
    io_command_motor(direction_left, command_stop, 0);
    io_command_motor(direction_right, command_stop, 0);

    sleep(DELAY);

    // Change direction on one relay
    if ( this.direction == direction_forward )
    {
        io_command_relay(direction == direction_right ? direction_right : direction_left, direction_backward);
    }

    if ( this.direction == direction_backward )
    {
        io_command_relay(direction == direction_right ? direction_left : direction_right, direction_forward);
    }

    // Do the turn
    io_command_motor(direction_left, command_start, this.speed);
    io_command_motor(direction_right, command_start, this.speed);

    sleep(2);

    // Stop
    io_command_motor(direction_left, command_stop, 0);
    io_command_motor(direction_right, command_stop, 0);

    // Reset the relays
    io_command_relay(direction_left, this.direction);
    io_command_relay(direction_right, this.direction);

    sleep(DELAY);

    // Continue forward
    io_command_motor(direction_left, this.motor, this.speed);
    io_command_motor(direction_right, this.motor, this.speed);
}
