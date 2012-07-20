
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "state.h"

struct state {
    struct state_table*     table;
    int                     states;
    char                    name[32];
    int                     debug;
};

error_code state_create(state_t* state_t, struct state_table* table, int states, char* name)
{
    struct state* state;

    state = malloc(sizeof(*state));

    if ( state == NULL )
        return err_OUT_OF_MEMORY;

    state->table = table;
    state->states = states;
    state->debug = 0;
    snprintf(state->name, sizeof(state->name), "%s", name);

    *state_t = state;

    return err_OK;
}

error_code state_destroy(state_t* state_t)
{
    struct state* state;

    if ( state_t == NULL )
        return err_WRONG_ARGUMENT;

    state = (struct state*)*state_t;

    if ( state == NULL )
        return err_WRONG_ARGUMENT;

    free(state);

    *state_t = NULL;

    return err_OK;
}

error_code state_debug(state_t state_t, int debug)
{
    struct state* state;

    state = (struct state*)state_t;

    if ( state == NULL )
        return err_WRONG_ARGUMENT;

    state->debug = debug;

    return err_OK;
}

error_code state_change(state_t* current_state_t, state_t new_state_t)
{
    struct state* state_old;
    struct state* state_new;

    if ( current_state_t == NULL )
        return err_WRONG_ARGUMENT;

    if ( new_state_t == NULL )
        return err_WRONG_ARGUMENT;

    state_new = (struct state*)new_state_t;
    state_old = (struct state*)*current_state_t;
    if ( state_new->debug )
        if ( state_old != NULL ) {
            if ( state_old->debug )
                printf("Changing state from: %s to: %s\n", state_old->name, state_new->name);
        } else
            printf("Changing state from: NULL to: %s\n", state_new->name);

    *current_state_t = new_state_t;

    return err_OK;
}

error_code state_equal(state_t state1_t, state_t state2_t)
{
    if ( state1_t == state2_t )
        return err_EQUAL;

    return err_NOT_EQUAL;
}

error_code state_next(struct message_queue* message_handler, state_t state_t)
{
    struct state* state;
    error_code result = err_NO_HANDLER;
    struct message_item msg_buff;
    int len = 0;
    int i = 0;

    state = (struct state*)state_t;

    if ( state == NULL )
        return err_WRONG_ARGUMENT;

    if ( message_handler == NULL )
        return err_WRONG_ARGUMENT;

    memset(&msg_buff, 0, sizeof(msg_buff) );
    len = sizeof(msg_buff);

    result = message_receive(message_handler, &msg_buff, &len);
    if ( SUCCESS(result) ) {
        i = 0;
        while ( i < state->states ) {
            if ( state->table[i].message == msg_buff.head.type ) {
                result = state->table[i].function( &msg_buff );
            }
            i++;
        }
    }

    return result;
}

error_code state_stash(struct message_item* message)
{
    return err_NOT_IMPLEMENTED;
}

error_code state_pop()
{
    return err_NOT_IMPLEMENTED;
}


