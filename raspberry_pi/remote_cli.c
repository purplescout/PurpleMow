
#include <string.h>
#include <stdio.h>
#include <stdlib.h>  // exit

#include "utils/list.h"
#include "thread.h"
#include "modules.h"
#include "error_codes.h"
#include "cli.h"


/**
 * @defgroup remote_cli Remote CLI
 * Remote CLI
 *
 * @ingroup cli
 */

/**
 * Remote CLI
 *
 * @ingroup remote_cli
 */
struct remote_cli {
    pthread_t       thread;
};

// functions
static error_code remote_cli_start(void* data);
static void* remote_cli_listen(void *data);

// cli commands
static int command_remote_cli(char *args, int (*print)(const char *format, ...));

// private variables
static struct remote_cli this;

/**
 * Initialize the remote CLI.
 *
 * @ingroup remote_cli
 *
 * @return  Success status
 */
error_code remote_cli_init()
{
    cli_register_command("remote_cli", command_remote_cli);

    module_register_to_phase(phase_START, remote_cli_start, NULL);

    return err_OK;
}

/**
 * Start the remote CLI
 *
 * @ingroup remote_cli
 *
 * @return  Success status
 */
static error_code remote_cli_start(void* data)
{
    error_code result;

    result = thread_start(&this.thread, remote_cli_listen);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

/**
 * Handles incoming messages.
 *
 * @ingroup remote_cli
 *
 * @param[in] data  Data to the thread
 *
 * @return          Return value from thread
 */
static void* remote_cli_listen(void *data)
{
    while ( 1 ) {
//        printf("remote_cli\n");
        sleep(10);
    }
}

/**
 * The command <b>remote_cli</b>
 *
 * @ingroup cli
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_remote_cli(char *args, int (*print)(const char *format, ...))
{
    return 1;
}

