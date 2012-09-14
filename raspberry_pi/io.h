#ifndef IO_H
#define IO_H

#include "error_codes.h"
#include "command.h"

error_code io_init();
error_code io_command_motor(enum direction direction, enum command command, int speed);
error_code io_command_relay(enum direction direction, enum direction command);
error_code io_command_read(enum sensor sensor, int *value);

#endif // IO_H
