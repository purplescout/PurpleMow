
#include <stdio.h>
#include <string.h>
#include <pthread.h>

#include "messages.h"
#include "communicator.h"
#include "raspi_io.h"

#define DELAY 1

enum msg_communicator {
    msg_forward,
    msg_backward,
    msg_left,
    msg_right,
    msg_speed,
    msg_stop,
};

struct communicator_message {
    enum msg_communicator   message;
    int                     data;
};

struct communicator {
    enum                direction direction;
    enum                command motor;
    int                 speed;
    struct message_item message_handle;
    pthread_t           thread;
};

static struct communicator this = { 0 };

static void* communicator_worker(void *threadid);

static void move(enum direction direction);
static void turn(enum direction direction);
static int command_move(char *args);

static error_code move_forward();
static error_code move_backward();
static error_code turn_left();
static error_code turn_right();
static error_code set_speed(int speed);
static error_code stop();

error_code communicator_init()
{
    this.direction = direction_undefined;
    this.motor = command_stop;
    this.speed = 255;

    cli_register_command("move", command_move);

    message_open(&this.message_handle, Q_COMMUNICATOR);

    return err_OK;
}

error_code communicator_start()
{
    int res;

    res = pthread_create(&this.thread, NULL, communicator_worker, NULL);

    if ( res != 0 ) {
        fprintf(stderr, "Failed to create thread\n");
        return err_THREAD;
    }

    return err_OK;
}

static void* communicator_worker(void *threadid)
{
    struct communicator_message msg;
    int len;
    error_code result;

    while ( 1 ) {
        memset(&msg, 0, sizeof(msg) );
        len = sizeof(msg);
        result = message_receive(&this.message_handle, &msg, &len);

        if ( SUCCESS(result) )
        {
            switch ( msg.message ) {
                case msg_forward:
                    move_forward();
                    break;
                case msg_backward:
                    move_backward();
                    break;
                case msg_left:
                    turn_left();
                    break;
                case msg_right:
                    turn_right();
                    break;
                case msg_speed:
                    set_speed(msg.data);
                    break;
                case msg_stop:
                    stop();
                    break;
                default:
                    break;
            }
        }
    }
}

error_code communicator_set_speed(int speed)
{
    struct communicator_message msg;
    msg.message = msg_speed;
    msg.data = speed;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

error_code communicator_stop()
{
    struct communicator_message msg;
    msg.message = msg_stop;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

error_code communicator_move_forward()
{
    struct communicator_message msg;
    msg.message = msg_forward;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

error_code communicator_move_backward()
{
    struct communicator_message msg;
    msg.message = msg_backward;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

error_code communicator_turn_left()
{
    struct communicator_message msg;
    msg.message = msg_left;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

error_code communicator_turn_right()
{
    struct communicator_message msg;
    msg.message = msg_right;
    message_send(&msg, sizeof(msg), Q_COMMUNICATOR);
    return err_OK;
}

static int command_move(char *args)
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
        char *c;
        int speed;
        c = strchr(args, ' ');
        if ( c != NULL ) {
            *c = '\0';
            c++;
            speed = atoi(c);
            communicator_set_speed(speed);
            printf("New speed: %d\n", this.speed);
        }
    } else {
        printf("Valid arguments: forward, backward, left, right, stop, speed [0-255]\n");
    }
    return 0;
}

static error_code move_forward()
{
    move(direction_forward);
    return err_OK;
}

static error_code move_backward()
{
    move(direction_backward);
    return err_OK;
}

static error_code turn_left()
{
    turn(direction_left);
    return err_OK;
}

static error_code turn_right()
{
    turn(direction_right);
    return err_OK;
}

static error_code set_speed(int speed)
{
    this.speed = speed > 255 ? 255 : speed;
    return err_OK;
}

static void move(enum direction direction)
{
    if ( direction != this.direction )
    {
        // Stop motors
        command_motor(direction_left, command_stop, 0);
        command_motor(direction_right, command_stop, 0);

        sleep(DELAY);

        // Change direction on relays
        command_relay(direction_left, direction);
        command_relay(direction_right, direction);

        sleep(DELAY);

        this.direction = direction;
    }

    // Move forward
    command_motor(direction_left, command_start, this.speed);
    command_motor(direction_right, command_start, this.speed);

    this.motor = command_start;
}

static error_code stop()
{
    command_motor(direction_left, command_stop, 0);
    command_motor(direction_right, command_stop, 0);

    this.motor = command_stop;
    return err_OK;
}

static void turn(enum direction direction)
{
    // Stop motors
    command_motor(direction_left, command_stop, 0);
    command_motor(direction_right, command_stop, 0);

    sleep(DELAY);

    // Change direction on one relay
    if ( this.direction == direction_forward )
    {
        command_relay(direction == direction_right ? direction_right : direction_left, direction_backward);
    }

    if ( this.direction == direction_backward )
    {
        command_relay(direction == direction_right ? direction_left : direction_right, direction_forward);
    }

    // Do the turn
    command_motor(direction_left, command_start, this.speed);
    command_motor(direction_right, command_start, this.speed);

    sleep(2);

    // Stop
    command_motor(direction_left, command_stop, 0);
    command_motor(direction_right, command_stop, 0);

    // Reset the relays
    command_relay(direction_left, this.direction);
    command_relay(direction_right, this.direction);

    sleep(DELAY);

    // Continue forward
    command_motor(direction_left, this.motor, this.speed);
    command_motor(direction_right, this.motor, this.speed);
}
