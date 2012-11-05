
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
 * @defgroup local_cli Local CLI
 * Local CLI
 *
 * @ingroup cli
 */

/**
 * Local CLI
 *
 * @ingroup local_cli
 */
struct local_cli {
    pthread_t       thread;
};

// functions
static error_code local_cli_start(void* data);
static void* local_cli_listen(void *data);
static error_code parse_command(char *command);

// private variables
static struct local_cli this;

/**
 * Initialize the local cli.
 *
 * @ingroup local_cli
 *
 * @return  Success status
 */
error_code local_cli_init()
{
    module_register_to_phase(phase_START, local_cli_start, NULL);

    return err_OK;
}

/**
 * Start the local cli
 *
 * @ingroup local_cli
 *
 * @return  Success status
 */
static error_code local_cli_start(void* data)
{
    error_code result;

    result = thread_start(&this.thread, local_cli_listen);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

/**
 * Handles incoming messages.
 *
 * @ingroup local_cli
 *
 * @param[in] data  Data to the thread
 *
 * @return          Return value from thread
 */
static void* local_cli_listen(void *data)
{
    char buffer[BUFFER_SIZE] = { 0 };

    while ( 1 ) {
        int result;
        printf("> ");
        fgets(buffer, sizeof(buffer), stdin);

        result = cli_execute_command(buffer);

        if ( result == err_UNKNOWN_COMMAND ) {
            printf("Unknown command: %s\n", buffer);
        }
    }
}

