#ifndef COMMUNICATOR_H
#define COMMUNICATOR_H

#include "error_codes.h"

error_code communicator_init();

error_code stop();
error_code move_forward();
error_code move_backward();
error_code turn_left();
error_code turn_right();


#endif // COMMUNICATOR_H
