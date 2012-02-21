
#include "communicator.h"

#define DELAY 1

enum direction {
    forward,
    backward,
    right,
    left,
    undefined,
};

enum command {
    start,
    stop,
};

struct communicator {
    enum direction direction;
    enum command motor;
};

struct communicator this = { 0 };

static void move(enum direction direction);
static void turn(enum direction direction);

void communicator_init()
{
    this.direction = undefined;
}

void move_forward()
{
    move(forward);
}

void move_backward()
{
    move(backward);
}

void turn_left()
{
    turn(left);
}

void turn_right()
{
    turn(right);
}

static void move(enum direction direction)
{
    if ( direction != this.direction )
    {
        command_motor(left, stop);
        command_motor(right, stop);

        sleep(DELAY);

        command_relay(left, direction);
        command_relay(right, direction);

        sleep(DELAY);

        this.direction = direction;
    }

    command_motor(left, start);
    command_motor(right, start);

    this.motor = start;
}

void stop()
{
    command_motor(left, stop);
    command_motor(right, stop);

    this.motor = stop;
}

static void turn(enum direction direction)
{
    command_motor(left, stop);
    command_motor(right, stop);

    sleep(DELAY);

    if ( this.direction == forward )
    {
        command_relay(direction == right ? right : left, backward);
    }

    if ( this.direction == backward )
    {
        command_relay(direction == right ? left : right, forward);
    }

    command_motor(left, start);
    command_motor(right, start);

    sleep(2);

    command_relay(left, this.direction);
    command_relay(right, this.direction);

    sleep(DELAY);

    command_motor(left, this.motor);
    command_motor(right, this.motor);
}
