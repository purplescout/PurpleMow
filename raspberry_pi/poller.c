
#include "error_codes.h"
#include "thread.h"
#include "poller.h"

// Thread
static void* poller_worker_sec(void *data);
static void* poller_worker_usec(void *data);

error_code poller_create(struct poller* poller, int sec, int usec,
        error_code worker(void *data), void *data)
{
    poller->enabled = 0;
    poller->sec = sec;
    poller->usec = usec;
    poller->worker = worker;
    poller->data = data;
    poller->initialized = 1;

    return err_OK;
}

error_code poller_start(struct poller* poller)
{
    // Sanity check before starting the thread
    if ( !poller->initialized )
        return err_NOT_INITIALIZED;

    if ( poller->worker == NULL )
        return err_NOT_INITIALIZED;

    poller->enabled = 1;

    if ( poller->sec > 0 )
        return thread_start_data(&poller->thread, poller_worker_sec, poller);
    if ( poller->usec > 0 )
        return thread_start_data(&poller->thread, poller_worker_usec, poller);

    return err_WRONG_ARGUMENT;
}

error_code poller_stop(struct poller* poller)
{
    poller->enabled = 0;
    return err_OK;
}

static void* poller_worker_sec(void *data)
{
    struct poller* poller = (struct poller*)data;

    while ( poller->enabled ) {
        poller->worker(poller->data);
        sleep(poller->sec);
    }
}
static void* poller_worker_usec(void *data)
{
    struct poller* poller = (struct poller*)data;

    while ( poller->enabled ) {
        poller->worker(poller->data);
        usleep(poller->usec);
    }
}

