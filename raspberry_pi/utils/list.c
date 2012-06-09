
#include <stdlib.h>

#include "list.h"

/**
 * @defgroup list List
 * Linked list.
 *
 * @ingroup utils
 */


/**
 * List item
 *
 * @ingroup list
 */
struct list_item {
    void*               data;
    struct list_item*   next;
    struct list_item*   previous;
};

/**
 * List
 *
 * @ingroup list
 */
struct list {
    struct list_item*   first;
    struct list_item*   last;
    int                 items;
};

/**
 * List iterator
 *
 * @ingroup list
 */
struct list_iterator {
    struct list_item*   item;
};


// private functions
static error_code list_create_item(struct list_item** item);
static error_code list_destroy_item(struct list_item* item);
static error_code list_add_after(struct list_item* existing, struct list_item* new);
static error_code list_add_before(struct list_item* existing, struct list_item* new);
static error_code list_create_iterator_internal_first(struct list* list, struct list_iterator* iterator);
static error_code list_create_iterator_internal_last(struct list* list, struct list_iterator* iterator);
static error_code list_destroy_iterator_internal(struct list_iterator* iterator);

/**
 * Create an empty linked list.
 *
 * @ingroup list
 *
 * @param[out] list     The linked list
 *
 * @return              Success status
 */
error_code list_create(list_t* list_t)
{
    struct list** list;

    list = (struct list**)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    *list = malloc(sizeof(**list));

    (*list)->first = NULL;
    (*list)->last = NULL;
    (*list)->items = 0;

    return err_OK;
}

/**
 * Destroy a linked list.
 *
 * @ingroup list
 *
 * @param[in] list      The linked list
 *
 * @return              Success status
 */
error_code list_destroy(list_t list_t)
{
    struct list* list;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    while( SUCCESS( list_remove_first(list_t) ) );

    free(list);

    return err_OK;
}

/**
 * Get the length of the list, ie current number of items.
 *
 * @ingroup list
 *
 * @param[in] list      The list
 * @param[out] length   Returned length
 *
 * @return              Success status
 */
error_code list_length(list_t list_t, int* length)
{
    struct list* list;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( length == NULL )
        return err_WRONG_ARGUMENT;

    *length = list->items;

    return err_OK;
}

/**
 * Create a list item.
 *
 * @ingroup list
 *
 * @param[out] item     Created item
 *
 * @return              Success status
 */
static error_code list_create_item(struct list_item** item)
{
    if( item == NULL )
        return err_WRONG_ARGUMENT;

    *item = malloc(sizeof(**item));

    if( *item == NULL )
        return err_OUT_OF_MEMORY;

    (*item)->data = NULL;
    (*item)->next = NULL;
    (*item)->previous = NULL;

    return err_OK;
}

/**
 * Destroy a list item.
 *
 * @ingroup list
 *
 * @param[in] item      Item to destroy
 *
 * @return              Success status
 */
static error_code list_destroy_item(struct list_item* item)
{
    free(item);

    return err_OK;
}

/**
 * Add a new item after an existing item.
 *
 * @ingroup list
 *
 * @param[in] existing      The existing item
 * @param[in] new           The new item
 *
 * @return                  Success status
 */
static error_code list_add_after(struct list_item* existing, struct list_item* new)
{
    if( existing == NULL )
        return err_WRONG_ARGUMENT;

    if( new == NULL )
        return err_WRONG_ARGUMENT;

    new->next = existing->next;

    if( existing->next != NULL )
        existing->next->previous = new;

    existing->next = new;
    new->previous = existing;

    return err_OK;
}

/**
 * Add a new item before an existing item.
 *
 * @ingroup list
 *
 * @param[in] existing      The existing item
 * @param[in] new           The new item
 *
 * @return                  Success status
 */
static error_code list_add_before(struct list_item* existing, struct list_item* new)
{
    if( existing == NULL )
        return err_WRONG_ARGUMENT;

    if( new == NULL )
        return err_WRONG_ARGUMENT;

    new->previous = existing->previous;

    if( existing->previous != NULL )
        existing->previous->next = new;

    existing->previous = new;
    new->next = existing;

    return err_OK;
}

/**
 * Add an item to the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 * @param[in] data      Data to add
 *
 * @return              Success status
 */
error_code list_add_last(list_t list_t, void* data)
{
    struct list* list;
    struct list_item* item;
    error_code status;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( data == NULL )
        return err_NO_ITEM;

    status = list_create_item(&item);
    if( FAILURE(status) )
        return status;

    item->data = data;

    if( list->first == NULL )
    {
        list->first = item;
        list->last = item;
    } else {
        list_add_after(list->last, item);
        list->last = item;
    }

    list->items++;

    return err_OK;
}

/**
 * Add an item in the beginning of the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 * @param[in] data      Data to add
 *
 * @return              Success status
 */
error_code list_add_first(list_t list_t, void* data)
{
    struct list* list;
    struct list_item* item;
    error_code status;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( data == NULL )
        return err_NO_ITEM;

    status = list_create_item(&item);
    if( FAILURE(status) )
        return status;

    item->data = data;

    if( list->first == NULL )
    {
        list->first = item;
        list->last = item;
    } else {
        list_add_before(list->first, item);
        list->first = item;
    }

    list->items++;

    return err_OK;
}

/**
 * Remove the first item from the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 *
 * @return              Success status
 */
error_code list_remove_first(list_t list_t)
{
    struct list* list;
    struct list_item* item;
    error_code status;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( list->first == NULL )
        return err_EMPTY_LIST;

    item = list->first;

    list->first = item->next;

    if( list->first == NULL )
        list->last = NULL;

    list->items--;

    status = list_destroy_item(item);
    if( FAILURE(status) )
        return status;

    return err_OK;
}

/**
 * Remove the last item in the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 *
 * @return              Success status
 */
error_code list_remove_last(list_t list_t)
{
    struct list* list;
    struct list_item* item;
    error_code status;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( list->last == NULL )
        return err_EMPTY_LIST;

    item = list->last;

    list->last = item->previous;

    if( list->last == NULL )
        list->first = NULL;

    list->items--;

    status = list_destroy_item(item);
    if( FAILURE(status) )
        return status;

    return err_OK;
}

/**
 * Get the first item in the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 * @param[out] data     Returned data
 *
 * @return              Success status
 */
error_code list_get_first(list_t list_t, void** data)
{
    struct list* list;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( data == NULL )
        return err_WRONG_ARGUMENT;

    if( list->first == NULL )
        return err_EMPTY_LIST;

    *data = list->first->data;

    return err_OK;
}

/**
 * Get the last item in the list.
 *
 * @ingroup list
 *
 * @param[in] list_t    The list
 * @param[out] data     Returned data
 *
 * @return              Success status
 */
error_code list_get_last(list_t list_t, void** data)
{
    struct list* list;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( data == NULL )
        return err_WRONG_ARGUMENT;

    if( list->last == NULL )
        return err_EMPTY_LIST;

    *data = list->last->data;

    return err_OK;
}

/**
 * Create an iterator.
 *
 * @ingroup list
 *
 * @param[out] iterator_t   The iterator
 *
 * @return                  Success status
 */
error_code list_create_iterator(list_iterator_t* iterator_t)
{
    struct list_iterator** iterator;

    iterator = (struct list_iterator**)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    *iterator = malloc(sizeof(**iterator));

    (*iterator)->item = NULL;

    return err_OK;
}

/**
 * Create an internal iterator pointing at the first item.
 *
 * @ingroup list
 *
 * @param[in] iterator      The iterator
 *
 * @return                  Success status
 */
static error_code list_create_iterator_internal_first(struct list* list, struct list_iterator* iterator)
{
    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    iterator->item = list->first;

    return err_OK;
}

/**
 * Create an internal iterator pointing at the last item.
 *
 * @ingroup list
 *
 * @param[in] iterator      The iterator
 *
 * @return                  Success status
 */
static error_code list_create_iterator_internal_last(struct list* list, struct list_iterator* iterator)
{
    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    iterator->item = list->last;

    return err_OK;
}

/**
 * Destroy an iterator.
 *
 * @ingroup list
 *
 * @param[in] iterator_t    The iterator
 *
 * @return                  Success status
 */
error_code list_destroy_iterator(list_iterator_t iterator_t)
{
    struct list_iterator** iterator;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    free(iterator);

    return err_OK;
}

/**
 * Destroy an internal iterator.
 *
 * @ingroup list
 *
 * @param[in] iterator      The iterator
 *
 * @return                  Success status
 */
static error_code list_destroy_iterator_internal(struct list_iterator* iterator)
{
    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    iterator->item = NULL;

    return err_OK;
}

/**
 * Set the iterator to point to the first item in the list.
 *
 * @ingroup list
 *
 * @param[in] list_t        The list
 * @param[out] iterator_t   The iterator
 *
 * @return                  Success status
 */
error_code list_set_iterator_first(list_t list_t, list_iterator_t iterator_t)
{
    struct list* list;
    struct list_iterator* iterator;

    list = (struct list*)list_t;
    iterator = (struct list_iterator*)iterator_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    if( list->first == NULL )
        return err_EMPTY_LIST;

    iterator->item = list->first;

    return err_OK;
}

/**
 * Set the iterator to point to the last item in the list.
 *
 * @ingroup list
 *
 * @param[in] list_t        The list
 * @param[out] iterator_t   Returned iterator
 *
 * @return                  Success status
 */
error_code list_set_iterator_last(list_t list_t, list_iterator_t iterator_t)
{
    struct list* list;
    struct list_iterator* iterator;

    list = (struct list*)list_t;
    iterator = (struct list_iterator*)iterator_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    if( list->last == NULL )
        return err_EMPTY_LIST;

    iterator->item = list->last;

    return err_OK;
}

/**
 * Move the iterator to the next item.
 *
 * @ingroup list
 *
 * @param[in,out] iterator_t    The iterator
 *
 * @return                      Success status
 */
error_code list_move_iterator_next(list_iterator_t iterator_t)
{
    struct list_iterator* iterator;

    iterator = (struct list_iterator*)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    iterator->item = iterator->item->next;

    if( iterator->item == NULL )
        return err_NO_MORE_ITEMS;

    return err_OK;
}

/**
 * Move the iterator to the previous item.
 *
 * @ingroup list
 *
 * @param[in,out] iterator_t    The iterator
 *
 * @return                      Success status
 */
error_code list_move_iterator_previous(list_iterator_t iterator_t)
{
    struct list_iterator* iterator;

    iterator = (struct list_iterator*)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    iterator->item = iterator->item->previous;

    if( iterator->item == NULL )
        return err_NO_MORE_ITEMS;

    return err_OK;
}

/**
 * Add an item before the position the iterator is pointing at.
 *
 * @ingroup list
 *
 * @param[in] iterator_t    The iterator
 * @param[in] data          The data
 *
 * @return                  Success status
 */
error_code list_add_before_iterator(list_iterator_t iterator_t, void* data)
{
    struct list_iterator* iterator;
    struct list_item* item;
    error_code status;

    iterator = (struct list_iterator*)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator->item == NULL )
        return err_NO_MORE_ITEMS;

    status = list_create_item(&item);
    if( FAILURE(status) )
        return status;

    if( iterator->item->previous != NULL )
        iterator->item->previous = item;

    item->previous = iterator->item->previous;
    item->next = iterator->item;

    iterator->item->previous = item;

    return err_OK;
}

/**
 * Add an item after the position the iterator is pointing at.
 *
 * @ingroup list
 *
 * @param[in] iterator_t    The iterator
 * @param[in] data          The data
 *
 * @return                  Success status
 */
error_code list_add_after_iterator(list_iterator_t iterator_t, void* data)
{
    struct list_iterator* iterator;
    struct list_item* item;
    error_code status;

    iterator = (struct list_iterator*)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator->item == NULL )
        return err_NO_MORE_ITEMS;

    status = list_create_item(&item);
    if( FAILURE(status) )
        return status;

    if( iterator->item->next != NULL )
        iterator->item->next = item;

    item->previous = iterator->item;
    item->next = iterator->item->next;

    iterator->item->next = item;

    return err_OK;
}

/**
 * Get the data the iterator is pointing at.
 *
 * @ingroup list
 *
 * @param[in] iterator_t    The iterator
 * @param[out] data         The data
 *
 * @return                  Success status
 */
error_code list_get_iterator_data(list_iterator_t iterator_t, void** data)
{
    struct list_iterator* iterator;

    iterator = (struct list_iterator*)iterator_t;

    if( iterator == NULL )
        return err_WRONG_ARGUMENT;

    if( iterator->item == NULL )
        return err_NO_MORE_ITEMS;

    if( data == NULL )
        return err_WRONG_ARGUMENT;

    *data = iterator->item->data;

    return err_OK;
}

/**
 * Add an item to a sorted list.
 * The comparator function should return err_LESS_THAN, err_GREATER_THAN or err_EQUAL.
 *
 * @ingroup list
 *
 * @param[in] list_t        The sorted list
 * @param[in] data          The data to insert
 * @param[in] comparator    The comparator
 *
 * @return                  Success status
 */
error_code list_add_ordered(list_t list_t, void* data, error_code (*comparator)(void* data1, void* data2))
{
    struct list* list;
    struct list_iterator iterator;
    struct list_item* item;
    error_code status;

    list = (struct list*)list_t;

    if( list == NULL )
        return err_WRONG_ARGUMENT;

    if( data == NULL )
        return err_WRONG_ARGUMENT;

    if( comparator == NULL )
        return err_WRONG_ARGUMENT;

    if( list->first == NULL )
        return err_EMPTY_LIST;

    list_create_iterator_internal_first(list, &iterator);

    do
    {
        status = comparator( data, iterator.item->data );

        if( status == err_EQUAL )
            status = err_ALREADY_IN_LIST;

        if( status == err_GREATER_THAN )
        {
            list_create_item(&item);
            item->data = data;
            list_add_before(iterator.item, item);
            status = err_OK;
        }

        iterator.item = iterator.item->next;

        if( iterator.item == NULL )
        {
            list_create_item(&item);
            item->data = data;
            list_add_after(list->last, item);
            list->last = item;
            status = err_OK;
        }
    } while( status == err_LESS_THAN );

    list_destroy_iterator_internal(&iterator);

    return status;
}
