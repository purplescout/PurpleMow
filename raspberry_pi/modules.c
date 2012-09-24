
#include <pthread.h>
#include <stdlib.h>

#include "error_codes.h"
#include "utils/list.h"
#include "modules.h"

/**
 * @defgroup modules Modules
 * Modules
 *
 * @ingroup purplemow
 */

struct function_item {
    void*                   data;
    error_code              (*function)();
};

struct phase_item {
    enum module_phase       phase;
    list_t                  list;
};

/**
 * Modules
 *
 * @ingroup modules
 */
struct modules {
    list_t                  list;
    pthread_mutex_t         list_mutex;
};

// private variables
static struct modules this;

// private functions
static error_code compare_phase(void* data1, void* data2);
static error_code module_create_phase(enum module_phase phase);
static error_code module_destroy_phase(enum module_phase phase);

/**
 * Initialize the modules.
 *
 * @ingroup modules
 *
 * @return  Success status
 */
error_code modules_init()
{
    int i;

    pthread_mutex_init(&this.list_mutex, NULL);

    list_create(&this.list);

    i = 0;
    while ( i < phase_LAST ) {
        module_create_phase(i);
        i++;
    }

    return err_OK;
}

error_code modules_run_phase(enum module_phase phase)
{
    struct function_item*   function_item;
    struct phase_item*      phase_item;
    struct phase_item       compare_item;
    list_iterator_t         function_iterator;
    list_iterator_t         phase_iterator;
    error_code              status;

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(this.list, &phase_iterator);
    list_set_iterator_first(phase_iterator);

    compare_item.phase = phase;
    status = list_find_item(phase_iterator, &compare_item, compare_phase);

    if( status == err_EQUAL ) {
        list_get_iterator_data(phase_iterator, (void*)&phase_item);
        list_create_iterator(phase_item->list, &function_iterator);

        list_set_iterator_first(function_iterator);

        while( SUCCESS( list_get_iterator_data(function_iterator, (void*)&function_item) ) ) {
            function_item->function(function_item->data);
            list_move_iterator_next(function_iterator);
        }
        list_destroy_iterator(function_iterator);
    }

    list_destroy_iterator(phase_iterator);

    pthread_mutex_unlock(&this.list_mutex);

    return status;
    return err_OK;
}

static error_code compare_phase(void* data1, void* data2)
{
    struct phase_item*  item1;
    struct phase_item*  item2;

    item1 = (struct phase_item*)data1;
    item2 = (struct phase_item*)data2;

    if( item1->phase < item2->phase )
        return err_LESS_THAN;
    if( item1->phase > item2->phase )
        return err_GREATER_THAN;
    return err_EQUAL;
}

static error_code module_create_phase(enum module_phase phase)
{
    struct phase_item*  new_phase;
    error_code          status;

    new_phase = malloc(sizeof(*new_phase));

    if ( new_phase == NULL )
        return err_OUT_OF_MEMORY;

    list_create(&new_phase->list);
    new_phase->phase = phase;

    pthread_mutex_lock(&this.list_mutex);

    status = list_add_last(this.list, new_phase);

    pthread_mutex_unlock(&this.list_mutex);

    return status;
}

static error_code module_destroy_phase(enum module_phase phase)
{
    return err_NOT_IMPLEMENTED;
}

/**
 * Register a module to a phase.
 *
 * @ingroup module
 *
 * @param[in] phase     Phase
 * @param[in] function  Callback function
 * @param[in] data      Data to be sent when calling the function
 *
 * @return              Success status
 */
error_code module_register_to_phase(enum module_phase phase, error_code(*function)(void* data), void* data)
{
    struct function_item*   new_function;
    struct phase_item*      phase_item;
    struct phase_item       compare_item;
    list_iterator_t         iterator;
    error_code              status;

    new_function = malloc(sizeof(*new_function));

    if( new_function == NULL )
        return err_OUT_OF_MEMORY;

    new_function->function = function;
    new_function->data = data;

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(this.list, &iterator);
    list_set_iterator_first(iterator);

    compare_item.phase = phase;
    status = list_find_item(iterator, &compare_item, compare_phase);

    if( status == err_EQUAL ) {
        list_get_iterator_data(iterator, (void*)&phase_item);
        status = list_add_last(phase_item->list, new_function);
    } else {
        free(new_function);
    }

    list_destroy_iterator(iterator);

    pthread_mutex_unlock(&this.list_mutex);

    return status;
}

/**
 * Unregister a module from a phase
 *
 * @ingroup module
 *
 * @param[in] phase     Phase
 * @param[in] function  Callback function
 *
 * @return              Success status
 */
error_code module_unregister_from_phase(enum module_phase phase, error_code(*function)(void* data))
{
    return err_NOT_IMPLEMENTED;
}

