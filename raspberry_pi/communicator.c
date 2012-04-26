
#include <stdio.h>
#include <string.h>

#include "communicator.h"
#include "raspi_io.h"

#define DELAY 1

struct communicator {
    enum direction direction;
    enum command motor;
    int speed;
};

static struct communicator this = { 0 };

static void move(enum direction direction);
static void turn(enum direction direction);
static int command_move(char *args);

error_code communicator_init()
{
    this.direction = direction_undefined;
    this.motor = command_stop;
    this.speed = 255;

    cli_register_command("move", command_move);

    return err_OK;
}

static int command_move(char *args)
{
    if ( strcmp("forward", args) == 0 ) {
        move_forward();
    } else if ( strcmp("backward", args) == 0 ) {
        move_backward();
    } else if ( strcmp("left", args) == 0 ) {
        turn_left();
    } else if ( strcmp("right", args) == 0 ) {
        turn_right();
    } else if ( strcmp("stop", args) == 0 ) {
        stop();
    } else if ( strncmp("speed", args, strlen("speed")) == 0 ) {
        char *c;
        int speed;
        c = strchr(args, ' ');
        if ( c != NULL ) {
            *c = '\0';
            c++;
            speed = atoi(c);
            this.speed = speed > 255 ? 255 : speed;
            printf("New speed: %d\n", this.speed);
        }
    } else {
        printf("Valid arguments: forward, backward, left, right, stop, speed [0-255]\n");
    }
    return 0;
}

error_code move_forward()
{
    move(direction_forward);
    return err_OK;
}

error_code move_backward()
{
    move(direction_backward);
    return err_OK;
}

error_code turn_left()
{
    turn(direction_left);
    return err_OK;
}

error_code turn_right()
{
    turn(direction_right);
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

error_code stop()
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
