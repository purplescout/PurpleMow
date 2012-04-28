#ifndef POLLER_H
#define POLLER_H

#include "error_codes.h"

enum poller_sleep_unit {
    poller_sleep_sec,
    poller_sleep_usec,
};

struct poller {
    int                     initialized;
    int                     enabled;
    enum poller_sleep_unit  sleep_unit;
    int                     sleep_time;
    pthread_t               thread;
    void*                   data;
    error_code              (*worker)(void* data);
};

error_code poller_create(struct poller* poller,
        enum poller_sleep_unit sleep_unit, int sleep_time,
        error_code worker(void *data), void *data);

error_code poller_start(struct poller* poller);
error_code poller_stop(struct poller* poller);

#endif // POLLER_H
