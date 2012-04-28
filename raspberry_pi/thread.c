
#include <pthread.h>
#include <stdio.h>

#include "error_codes.h"
#include "thread.h"

error_code thread_start(pthread_t *thread, void* work(void *data))
{
    int result;

    if ( thread == NULL || work == NULL ) {
        return err_WRONG_ARGUMENT;
    }

    result = pthread_create(thread, NULL, work, NULL);

    if ( result != 0 ) {
        fprintf(stderr, "Failed to create thread\n");
        return err_THREAD;
    }

    return err_OK;
}

