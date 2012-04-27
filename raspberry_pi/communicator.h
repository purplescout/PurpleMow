#ifndef COMMUNICATOR_H
#define COMMUNICATOR_H

#include "error_codes.h"

error_code communicator_init();
error_code communicator_start();

error_code communicator_stop();
error_code communicator_move_forward();
error_code communicator_move_backward();
error_code communicator_turn_left();
error_code communicator_turn_right();


#endif // COMMUNICATOR_H
