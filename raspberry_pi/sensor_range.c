#include <string.h>

#include "thread.h"
#include "error_codes.h"
#include "communicator.h"
#include "messages.h"
#include "poller.h"

#define POLL_INTERVAL   500000

struct sensor_range {
    struct message_item message_handle;
    pthread_t           thread;
    struct poller       poller;
};

// Thread
static void* sensor_range_worker(void *data);

// Poller
static error_code sensor_range_poll(void *data);

static struct sensor_range this;

error_code sensor_range_init()
{
    error_code result;

    result = message_open(&this.message_handle, Q_SENSOR_RANGE);

    if ( FAILURE(result) )
        return result;

    result = poller_create(&this.poller, poller_sleep_usec, POLL_INTERVAL, sensor_range_poll, NULL);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}

error_code sensor_range_start()
{
    error_code result;

    result = thread_start(&this.thread, sensor_range_worker);

    if ( FAILURE(result) )
        return result;

    result = poller_start(&this.poller);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}

static void* sensor_range_worker(void *data)
{
    struct msg_sensor_data msg;
    int len;
    error_code result;

    while ( 1 ) {
        memset(&msg, 0, sizeof(msg) );
        len = sizeof(msg);
        result = message_receive(&this.message_handle, &msg, &len);

        if ( SUCCESS(result) ) {
        }
    }
}

static error_code sensor_range_poll(void *data)
{
}
