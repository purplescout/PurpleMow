#ifndef CLI_H
#define CLI_H

int cli_init();
int cli_start();

int cli_register_command(char *command, int (*function)(char *arg));
int cli_unregister_command(char *command);

#endif // CLI_H
