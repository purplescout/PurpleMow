
#include <string.h>
#include <stdio.h>
#include <stdlib.h>  // exit

#include "utils/list.h"
#include "thread.h"
#include "modules.h"
#include "error_codes.h"
#include "cli.h"

#define BUFFER_SIZE 256

/**
 * @defgroup cli CLI
 * CLI
 *
 * @ingroup purplemow
 */

/**
 * A CLI item.
 *
 * @ingroup cli
 */
struct cli_item
{
    char                command[32];
    int                 (*function)(char *arg, int (*print)(const char *format, ...));
};

/**
 * CLI
 *
 * @ingroup cli
 */
struct cli {
    list_t          list;
    pthread_t       thread;
    pthread_mutex_t list_mutex;
};

// functions
static error_code cli_start(void* data);
static void* cli_listen(void *data);
static error_code parse_command(char *command);
static error_code list_add(struct cli_item* item);
static error_code list_remove(char *command, struct cli_item** cmd);

// cli commands
static int command_help(char *args, int (*print)(const char *format, ...));
static int command_exit(char *args, int (*print)(const char *format, ...));
static int command_echo(char *args, int (*print)(const char *format, ...));

// private variables
static struct cli this;

/**
 * Initialize the cli.
 *
 * @ingroup cli
 *
 * @return  Success status
 */
error_code cli_init()
{
    pthread_mutex_init(&this.list_mutex, NULL);

    list_create(&this.list);

    cli_register_command("?", command_help);
    cli_register_command("help", command_help);
    cli_register_command("echo", command_echo);
    cli_register_command("exit", command_exit);

    module_register_to_phase(phase_START, cli_start, NULL);

    return err_OK;
}

/**
 * Start the cli
 *
 * @ingroup cli
 *
 * @return  Success status
 */
static error_code cli_start(void* data)
{
    error_code result;

    result = thread_start(&this.thread, cli_listen);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

/**
 * Handles incoming messages.
 *
 * @ingroup cli
 *
 * @param[in] data  Data to the thread
 *
 * @return          Return value from thread
 */
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

/**
 * Comparator function, compares the command name of two cli_items.
 *
 * @ingroup cli
 *
 * @param[in] data1     First cli_item
 * @param[in] data2     Second cli_item
 *
 * @return              Success status
 */
static error_code compare_command(void* data1, void* data2)
{
    struct cli_item* item1;
    struct cli_item* item2;
    int status;

    item1 = (struct cli_item*)data1;
    item2 = (struct cli_item*)data2;

    status = strcmp(item1->command, item2->command);

    if( status < 0 )
        return err_LESS_THAN;
    if( status > 0 )
        return err_GREATER_THAN;
    return err_EQUAL;
}

/**
 * Parse a command and execute registered callback function.
 *
 * @ingroup cli
 *
 * @param[in] command   Command to parse
 *
 * @return              Success status
 */
static error_code parse_command(char *command)
{
    int size;
    char* args = "";
    int i;
    struct cli_item command_item;
    struct cli_item* item;
    list_iterator_t iterator;
    error_code status;

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

    snprintf(command_item.command, sizeof(command_item.command), "%s", command);

    list_create_iterator(this.list, &iterator);

    pthread_mutex_lock(&this.list_mutex);
    list_set_iterator_first(iterator);

    list_find_item(iterator, &command_item, compare_command);
    pthread_mutex_unlock(&this.list_mutex);

    status = list_get_iterator_data(iterator, (void*)&item);

    list_destroy_iterator(iterator);

    if( status == err_OK ) {
        item->function(args, printf);
    } else {
        return err_UNKNOWN_COMMAND;
    }

    return err_OK;
}

/**
 * Add a cli item to the command list.
 *
 * @ingroup cli
 *
 * @param[in] item  Item to be added
 *
 * @return          Success status
 */
static error_code list_add(struct cli_item* item)
{
    error_code status;

    pthread_mutex_lock(&this.list_mutex);

    status = list_add_ordered(this.list, item, compare_command);

    pthread_mutex_unlock(&this.list_mutex);

    return status;
}

/**
 * Remove a command from the command list.
 * Returns the found item.
 *
 * @ingroup cli
 *
 * @param[in] command   Command to remove
 * @param[out] cmd      Found cli_item
 *
 * @return              Success status
 */
static error_code list_remove(char *command, struct cli_item** cmd)
{
    struct cli_item item;
    list_iterator_t iterator;
    error_code status;

    if( cmd == NULL )
        return err_WRONG_ARGUMENT;

    snprintf(item.command, sizeof(item.command), "%s", command);

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(&this.list, &iterator);

    status = list_find_item(iterator, &item, compare_command);

    if( status == err_EQUAL )
        list_remove_at_iterator(iterator, (void**)cmd);

    pthread_mutex_unlock(&this.list_mutex);

    list_destroy_iterator(iterator);

    return status;
}

/**
 * Register a command with a callback function.
 *
 * @ingroup cli
 *
 * @param[in] command   Command to register
 * @param[in] function  Callback function
 *
 * @return              Success status
 */
error_code cli_register_command(char *command, int (*function)(char *arg, int (*print)(const char *format, ...)))
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

/**
 * Unregister a command and remove it from the command list.
 *
 * @ingroup cli
 *
 * @param[in] command   Command to unregister
 *
 * @return              Success status
 */
error_code cli_unregister_command(char *command)
{
    struct cli_item* cmd;
    error_code status;

    status = list_remove(command, &cmd);

    if( SUCCESS( status ) )
        free(cmd);

    return status;
}

/**
 * The command <b>help<b>, show a list of all registered commands.
 *
 * @private
 * @ingroup cli
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_help(char *args, int (*print)(const char *format, ...))
{
    list_iterator_t iterator;
    struct cli_item *item;

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(this.list, &iterator);

    list_set_iterator_first(iterator);

    while( SUCCESS( list_get_iterator_data(iterator, (void*)&item) ) ) {
        print("%s\n", item->command);
        list_move_iterator_next(iterator);
    }

    pthread_mutex_unlock(&this.list_mutex);

    return 0;
}

/**
 * The command <b>exit</b>, exits the program.
 *
 * @ingroup cli
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_exit(char *args, int (*print)(const char *format, ...))
{
    exit(0);

    return 1;
}

/**
 * The command <b>echo</b>, echo back the argument.
 *
 * @ingroup cli
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_echo(char *args, int (*print)(const char *format, ...))
{
    print("%s\n", args);

    return 0;
}

/**
 * Parse args for the first number and returns it in an int.
 * The number must have a space in front of it and either a space or EOL after it.
 *
 * @ingroup cli
 *
 * @param[in] args  Argument to parse
 *
 * @return          The found value or 0
 */
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

