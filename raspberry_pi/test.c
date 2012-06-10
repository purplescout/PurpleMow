
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "error_codes.h"
#include "test.h"
#include "utils/list.h"

/**
 * @defgroup test Test
 * Test file.
 *
 * @ingroup purplemow
 */

/**
 * Initialize the test.
 *
 * @ingroup test
 *
 * @return          Success status
 */
error_code test_init()
{
    list_t list;
    list_iterator_t iterator;
    char* a = "a";
    char* b = "b";
    char* c = "c";
    char* d = "d";
    char* get;
    int i;

    list_create(&list);
    list_add_last(list, a);
    list_add_last(list, b);
    list_add_last(list, c);
    list_add_last(list, d);

    list_length(list, &i);
    printf("List length: %d\n", i);

    list_create_iterator(list, &iterator);

    list_set_iterator_first(iterator);

    while( SUCCESS( list_get_iterator_data(iterator, (void*)&get)) ) {
        printf("Got: %c\n", *get);
        list_move_iterator_next(iterator);
    }

    list_destroy_iterator(iterator);
    list_destroy(list);

    return err_OK;
}

/**
 * Start the test.
 *
 * @ingroup test
 *
 * @return          Success status
 */
error_code test_start()
{
    return err_OK;
}

