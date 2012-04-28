
#include "error_codes.h"
#include "thread.h"
#include "poller.h"

// Thread
static void* poller_worker(void *data);

error_code poller_create(struct poller* poller,
        enum poller_sleep_unit sleep_unit, int sleep_time,
        error_code worker(void *data), void *data)
{
    poller->enabled = 0;
    poller->initialized = 1;

    poller->sleep_unit = sleep_unit;
    poller->sleep_time = sleep_time;
    poller->worker = worker;
    poller->data = data;

    return err_OK;
}

error_code poller_start(struct poller* poller)
{
    // Sanity check before starting the thread
    if ( !poller->initialized )
        return err_NOT_INITIALIZED;

    if ( poller->worker == NULL )
        return err_NOT_INITIALIZED;

    if ( poller->sleep_time == 0 )
        return err_WRONG_ARGUMENT;

    poller->enabled = 1;

    return thread_start_data(&poller->thread, poller_worker, poller);
}

error_code poller_stop(struct poller* poller)
{
    poller->enabled = 0;
    return err_OK;
}

static void* poller_worker(void *data)
{
    struct poller* poller = (struct poller*)data;

    while ( poller->enabled ) {
        poller->worker(poller->data);

        switch ( poller->sleep_unit ) {
            case poller_sleep_sec:
                sleep(poller->sleep_time);
                break;
            case poller_sleep_usec:
                usleep(poller->sleep_time);
                break;
        }
    }
}

