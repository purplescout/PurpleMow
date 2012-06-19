#ifndef COMMUNICATOR_H
#define COMMUNICATOR_H

#include "error_codes.h"
#include "command.h"
#include "messages.h"

enum msg_communicator {
    msg_communicator_forward,
    msg_communicator_backward,
    msg_communicator_left,
    msg_communicator_right,
    msg_communicator_speed,
    msg_communicator_stop,
    msg_communicator_sensor,
};

// Messages

// sensor_data
struct message_sensor_data_body {
    enum sensor     sensor;
    int             value;
};

struct message_sensor_data {
    struct message_head                 head;
    struct message_sensor_data_body     body;
};

// communicator
struct message_communicator_body {
    enum msg_communicator   message;
    enum sensor             sensor;
    enum queue              queue;
    int                     data;
};

struct message_communicator {
    struct message_head                 head;
    struct message_communicator_body    body;
};

// Public functions
error_code communicator_init();

error_code communicator_stop();
error_code communicator_move_forward();
error_code communicator_move_backward();
error_code communicator_turn_left();
error_code communicator_turn_right();
error_code communicator_set_speed();

error_code communicator_read(enum sensor sensor, enum queue rsp_queue);

#endif // COMMUNICATOR_H
