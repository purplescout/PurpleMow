#ifndef RASPI_IO_H
#define RASPI_IO_H

#include "error_codes.h"
#include "command.h"

error_code purple_io_init();
error_code command_motor(enum direction direction, enum command command, int speed);
error_code command_relay(enum direction direction, enum direction command);

#endif // RASPI_IO_H
