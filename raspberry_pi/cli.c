
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <pthread.h>

#include "cli.h"

#define BUFFER_SIZE 256

static void* cli_listen(void *threadid);
static int parse_command(char *command);

static pthread_t thread;

int cli_init()
{
    return 0;
}

int cli_start()
{
    int res;

    res = pthread_create(&thread, NULL, cli_listen, NULL);

    if ( res != 0 )
    {
        fprintf(stderr, "Failed to create thread\n");
        return -1;
    }

    return 0;
}

static void* cli_listen(void *threadid)
{
    char buffer[BUFFER_SIZE] = { 0 };

    while ( 1 )
    {
        printf("> ");
        fgets(buffer, sizeof(buffer), stdin);

        parse_command(buffer);
    }
}

static int parse_command(char *command)
{
    int size;

    size = strlen(command);

    // strip off ending white spaces
    while ( command[size-1] == '\n' ||
            command[size-1] == '\t' ||
            command[size-1] == ' ' )
    {
        command[size-1] = '\0';
        size--;
    }

    printf("Got command (%d): %s\n", size, command);

    if ( strcmp(command, "exit") == 0)
    {
        exit(0);
    }
}
