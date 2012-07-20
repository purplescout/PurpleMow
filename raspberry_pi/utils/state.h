#ifndef STATE_H
#define STATE_H

#include "../error_codes.h"
#include "../messages.h"

#define STATE_TABLE_SIZE(table) sizeof(table)/sizeof((table)[0])

typedef void* state_t;

struct state_table {
    enum msg_type           message;
    error_code              (*function)(struct message_item* message);
};

error_code state_create(state_t* state, struct state_table* table, int states, char* name);
error_code state_next(struct message_queue* message_handler, state_t state);
error_code state_change(state_t* current_state, state_t new_state);
error_code state_equal(state_t state1, state_t state2);
error_code state_debug(state_t state, int debug);

error_code state_stash(struct message_item* message);
error_code state_pop();

#endif // STATE
