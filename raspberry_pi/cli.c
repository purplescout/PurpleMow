
#include <string.h>
#include <stdio.h>
#include <stdlib.h>  // exit

#include "thread.h"
#include "error_codes.h"
#include "cli.h"

#define BUFFER_SIZE 256

struct cli_item
{
    char                command[32];
    int                 (*function)(char *arg);
    struct cli_item*    next;
    struct cli_item*    prev;
};

struct cli {
    pthread_t       thread;
    pthread_mutex_t list_mutex;
};

// functions
static void* cli_listen(void *data);
static int parse_command(char *command);
static error_code list_add(struct cli_item* item);
static error_code list_remove(char *command);

// cli commands
static int command_help(char *args);
static int command_exit(char *args);
static int command_echo(char *args);

// private variables
static struct cli_item* list_head;

static struct cli this;

error_code cli_init()
{
    pthread_mutex_init(&this.list_mutex, NULL);
    list_head = NULL;

    cli_register_command("?", command_help);
    cli_register_command("help", command_help);
    cli_register_command("echo", command_echo);
    cli_register_command("exit", command_exit);

    return err_OK;
}

error_code cli_start()
{
    error_code result;

    result = thread_start(&this.thread, cli_listen);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

static void* cli_listen(void *data)
{
    char buffer[BUFFER_SIZE] = { 0 };

    while ( 1 ) {
        int result;
        printf("> ");
        fgets(buffer, sizeof(buffer), stdin);

        result = parse_command(buffer);

        if ( result == err_UNKNOWN_COMMAND ) {
            printf("Unknown command: %s\n", buffer);
        }
    }
}

static int parse_command(char *command)
{
    int size;
    struct cli_item* current;
    int (*function)(char *arg) = NULL;
    char* args = "";
    int i;

    size = strlen(command);

    // strip off ending white spaces
    while ( command[size-1] == '\n' ||
            command[size-1] == '\t' ||
            command[size-1] == ' ' )
    {
        command[size-1] = '\0';
        size--;
    }

    if ( strlen(command) == 0 ) {
        return err_OK;
    }

    i = 0;
    while ( command[i] != ' ' ) {
        i++;
    }

    if ( i+1 < size ) {
        command[i] = '\0';
        args = &command[i+1];
    }

    pthread_mutex_lock(&this.list_mutex);

    current = list_head;
    while ( current != NULL ) {
        if ( strcmp(command, current->command) == 0) {
            function = current->function;
            current = NULL;
        }
        else {
            current = current->next;
        }
    }

    pthread_mutex_unlock(&this.list_mutex);

    if ( function == NULL ) {
        return err_UNKNOWN_COMMAND;
    }

    function(args);

    return err_OK;
}

static error_code list_add(struct cli_item* item)
{
    struct cli_item* current;
    int result = err_OK;

    pthread_mutex_lock(&this.list_mutex);

    if ( list_head == NULL )
    {
        // list is empty
        list_head = item;
        list_head->next = NULL;
        list_head->prev = NULL;
    }
    else
    {
        current = list_head;
        do
        {
            int cmp = strcmp(item->command, current->command);
            if ( cmp == 0 )
            {
                // command already reigstered
                current = NULL;
                result = err_ALREADY_REGISTERED;
            }
            else if ( cmp < 0 )
            {
                // insert item before current
                item->next = current;
                item->prev = current->prev;
                if ( current->prev != NULL )
                {
                    current->prev->next = item;
                }
                current->prev = item;
                if ( current == list_head )
                {
                    list_head = item;
                }
                current = NULL;
            }
            else
            {
                if ( current->next == NULL )
                {
                    // last item, insert item after current
                    current->next = item;
                    item->prev = current;
                    item->next = NULL;
                    current = NULL;
                }
                else
                {
                    current = current->next;
                }
            }
        } while ( current != NULL );
    }

    pthread_mutex_unlock(&this.list_mutex);

    return result;
}

static error_code list_remove(char *command)
{
    struct cli_item *current;
    int result = err_NOT_REGISTERED;

    pthread_mutex_lock(&this.list_mutex);

    current = list_head;
    while ( current != NULL )
    {
        if ( strcmp(current->command, command) == 0 )
        {
            // unlink this item
            if ( current->prev != NULL )
                current->prev->next = current->next;
            if ( current->next != NULL )
                current->next->prev = current->prev;
            result = err_OK;
            current = NULL;
        }
    }

    pthread_mutex_unlock(&this.list_mutex);

    return result;
}

error_code cli_register_command(char *command, int(*function)(char *arg))
{
    struct cli_item* new_cmd;

    new_cmd = malloc(sizeof(*new_cmd));

    if ( new_cmd == NULL )
    {
        return err_OUT_OF_MEMORY;
    }

    snprintf(new_cmd->command, sizeof(new_cmd->command), "%s", command);
    new_cmd->function = function;

    return list_add(new_cmd);
}

error_code cli_unregister_command(char *command)
{
    return list_remove(command);
}

static int command_help(char *args)
{
    struct cli_item *current;

    pthread_mutex_lock(&this.list_mutex);

    current = list_head;
    while ( current != NULL )
    {
        printf("%s\n", current->command);
        current = current->next;
    }

    pthread_mutex_unlock(&this.list_mutex);

    return 0;
}

static int command_exit(char *args)
{
    exit(0);

    return 1;
}

static int command_echo(char *args)
{
    printf("%s\n", args);

    return 0;
}

int cli_read_int(char *args)
{
    char *start;
    char *end;
    int value;

    start = strchr(args, ' ');

    if ( start == NULL ) {
        // nothing in string
        return 0;
    }

    end = strchr(start+1, ' ');

    if ( end != NULL ) {
        // temporarily end the string here
        *end = '\0';
    }

    value = atoi(start+1);

    if ( end != NULL ) {
        // temporarily end the string here
        *end = ' ';
    }

    return value;
}

