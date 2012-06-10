
#ifndef LIST_H
#define LIST_H

#include "../error_codes.h"

typedef void* list_t;
typedef void* list_iterator_t;

error_code list_create(list_t* list);
error_code list_destroy(list_t list);
error_code list_length(list_t list, int* length);

error_code list_add_last(list_t list, void* data);
error_code list_add_first(list_t list, void* data);
error_code list_add_ordered(list_t list, void* data, error_code (*comparator)(void* data1, void* data2));

error_code list_remove_first(list_t list);
error_code list_remove_last(list_t list);

error_code list_get_first(list_t list, void** data);
error_code list_get_last(list_t list, void** data);

error_code list_create_iterator(list_t list, list_iterator_t* iterator);
error_code list_destroy_iterator(list_iterator_t iterator);

error_code list_set_iterator_first(list_iterator_t iterator);
error_code list_set_iterator_last(list_iterator_t iterator);

error_code list_move_iterator_next(list_iterator_t iterator);
error_code list_move_iterator_previous(list_iterator_t iterator);

error_code list_add_before_iterator(list_iterator_t iterator, void* data);
error_code list_add_after_iterator(list_iterator_t iterator, void* data);

error_code list_remove_at_iterator(list_iterator_t iterator, void** data);

error_code list_get_iterator_data(list_iterator_t iterator, void** data);

error_code list_find_item(list_iterator_t iterator, void* data, error_code (*comparator)(void* data1, void* data2));
error_code list_find_item_reverse(list_iterator_t iterator, void* data, error_code (*comparator)(void* data1, void* data2));


#endif // LIST_H
