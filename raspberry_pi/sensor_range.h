#ifndef SENSOR_RANGE_H
#define SENSOR_RANGE_H

#include <pthread.h>

#include "error_codes.h"
#include "poller.h"
#include "messages.h"

/**
 * @ingroup sensor_range
 */
struct sensor_range {
    struct message_queue    message_handle;
    pthread_t               thread;
    struct poller           poller;
    int                     state;
    int                     queue;
};

error_code sensor_range_init(struct sensor_range* sensor);
error_code sensor_range_start(struct sensor_range* this);

#endif // SENSOR_RANGE_H
