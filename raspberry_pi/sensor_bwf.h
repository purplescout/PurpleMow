#ifndef SENSOR_BWF_H
#define SENSOR_BWF_H

#include <pthread.h>

#include "error_codes.h"
#include "poller.h"
#include "messages.h"

/**
 * @ingroup sensor_bwf
 */
struct sensor_bwf {
    struct message_queue    message_handle;
    pthread_t               thread;
    struct poller           poller;
    int                     state;
    int                     queue;
};

error_code sensor_bwf_init(struct sensor_bwf* sensor);
error_code sensor_bwf_start(struct sensor_bwf* this);

#endif // SENSOR_BWF_H
