
#include <pthread.h>
#include <stdio.h>

#include "error_codes.h"
#include "thread.h"

/**
 * @defgroup thread Thread
 * Threads.
 *
 * @ingroup purplemow
 */

/**
 * Create and start a thread.
 *
 * @ingroup thread
 *
 * @param[in] thread        Pointer to a pthread_t
 * @param[in] work          Function the thread should run
 *
 * @return                  Success status
 */
error_code thread_start(pthread_t *thread, void* work(void *data))
{
    return thread_start_data(thread, work, NULL);
}

/**
 * Create and start a thread with an argument.
 *
 * @ingroup thread
 *
 * @param[in] thread        Pointer to a pthread_t
 * @param[in] work          Function the thread should run
 * @param[in] data          Argument to the thread
 *
 * @return                  Success status
 */
error_code thread_start_data(pthread_t *thread, void* work(void *data), void* data)
{
    int result;

    if ( thread == NULL || work == NULL ) {
        return err_WRONG_ARGUMENT;
    }

    result = pthread_create(thread, NULL, work, data);

    if ( result != 0 ) {
        fprintf(stderr, "Failed to create thread\n");
        return err_THREAD;
    }

    return err_OK;
}

