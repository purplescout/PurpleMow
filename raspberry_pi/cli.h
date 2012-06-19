#ifndef CLI_H
#define CLI_H

#include "error_codes.h"

error_code cli_init();

error_code cli_register_command(char *command, int (*function)(char *arg));
error_code cli_unregister_command(char *command);

int cli_read_int(char *args);

#endif // CLI_H
