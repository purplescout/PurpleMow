#ifndef THREAD_H
#define THREAD_H

#include <pthread.h>

#include "error_codes.h"

error_code thread_start(pthread_t *thread, void* work(void *data));

#endif // THREAD_H
