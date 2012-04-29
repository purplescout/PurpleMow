
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "error_codes.h"
#include "thread.h"
#include "test_thread.h"
#include "messages.h"
#include "cli.h"

struct test_thread {
    pthread_t               thread;
    struct message_queue    message_handle;
};

// private functions
static void* worker(void *data);

// cli commands
static int command_sendmsg(char* args);

// private variables
static pthread_t thread;
static pthread_mutex_t list_mutex;


static struct test_thread this;

error_code test_thread_init()
{
    cli_register_command("send", command_sendmsg);
    message_open(&this.message_handle, Q_TEST);

    return err_OK;
}

error_code test_thread_start()
{
    error_code result;

    result = thread_start(&this.thread, worker);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

static void* worker(void *data)
{
    struct message_item msg;
    int len;
    error_code result;

    while ( 1 )
    {
        len = sizeof(msg.body);
        result = message_receive(&this.message_handle, &msg, &len);

        if ( SUCCESS(result) ) {
            printf("Got (in test thread): %s\n", msg.body.data);
        }
    }
}

static int command_sendmsg(char* args)
{
    struct message_item msg;

    if ( strlen(args) > 0 ) {
        printf("Sending to thread: %s\n", args);
        msg.head.type = MSG_TEST;
        msg.head.length = strlen(args) + 1 + sizeof(msg.head);
        strncpy(msg.body.data, args, sizeof(msg.body.data) );
        message_send(&msg, Q_TEST);
    }
    return 0;
}
