#ifndef COMMUNICATOR_H
#define COMMUNICATOR_H

#include "error_codes.h"
#include "command.h"
#include "messages.h"

struct msg_sensor_data {
    enum msg_type   type;
    enum sensor     sensor;
    int             value;
};

error_code communicator_init();
error_code communicator_start();

error_code communicator_stop();
error_code communicator_move_forward();
error_code communicator_move_backward();
error_code communicator_turn_left();
error_code communicator_turn_right();
error_code communicator_set_speed();

error_code communicator_read(enum sensor sensor, enum queue rsp_queue);

#endif // COMMUNICATOR_H
