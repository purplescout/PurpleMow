#ifndef POLLER_H
#define POLLER_H

#include "error_codes.h"

struct poller {
    int         initialized;
    int         enabled;
    int         usec;
    int         sec;
    pthread_t   thread;
    void*       data;
    error_code  (*worker)(void* data);
};

error_code poller_create(struct poller* poller, int sec, int usec,
        error_code worker(void *data), void *data);

error_code poller_start(struct poller* poller);
error_code poller_stop(struct poller* poller);

#endif // POLLER_H
