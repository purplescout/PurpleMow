
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <pthread.h>

#include "test_thread.h"
#include "messages.h"
#include "cli.h"

// private functions
static void* worker(void *threadid);

// cli commands
static int command_sendmsg(char* args);

// private variables
static pthread_t thread;
static pthread_mutex_t list_mutex;

static struct message_item message_handle;

int test_thread_init()
{
    cli_register_command("send", command_sendmsg);
    message_open(&message_handle, Q_TEST);

    return 0;
}

int test_thread_start()
{
    int res;

    res = pthread_create(&thread, NULL, worker, NULL);

    if ( res != 0 )
    {
        fprintf(stderr, "Failed to create thread\n");
        return -1;
    }

    return 0;
}

static void* worker(void *threadid)
{
    char buffer[MESSAGE_SIZE] = { 0 };
    int len;
    int result;

    while ( 1 )
    {
        len = sizeof(buffer);
        result = message_receive(&message_handle, buffer, &len);

        if (result == 0)
        {
            printf("Got (in test thread): %s\n", buffer);
        }
    }
}

static int command_sendmsg(char* args)
{
    if ( strlen(args) > 0 )
    {
        printf("Sending to thread: %s\n", args);
        message_send(args, strlen(args) + 1, Q_TEST);
    }
    return 0;
}
