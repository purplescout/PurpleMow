#ifndef MESSAGES_H
#define MESSAGES_H

#include <mqueue.h>

#include "error_codes.h"

#define MAX_MESSAGE_SIZE 128

#define message_create(in_var, in_msg, in_type) \
    (in_var).head.type = (in_type); \
    (in_var).head.length = sizeof(in_msg);

enum queue {
    Q_MAIN,
    Q_TEST,
    Q_COMMUNICATOR,
    Q_SENSOR_RANGE,
    Q_LAST,
};

enum msg_type {
    MSG_COMMUNICATOR,
    MSG_SENSOR_DATA,
    MSG_SENSOR_DECISION,
    MSG_TEST,
};

enum queue_prio {
    PRIO_HIGH_01    = 9,
    PRIO_HIGH       = 10,
    PRIO_HIGH_1     = 11,
    PRIO_HIGH_2     = 12,

    PRIO_NORMAL_0   = 19,
    PRIO_NORMAL     = 20,
    PRIO_NORMAL_1   = 21,
    PRIO_NORMAL_2   = 22,

    PRIO_LOW_0      = 29,
    PRIO_LOW        = 30,
    PRIO_LOW_1      = 31,
    PRIO_LOW_2      = 32,
};

struct message_queue {
    enum queue  queue_number;
    char        name[32];
    mqd_t       queue;
};

struct message_head {
    enum msg_type   type;
    int             length;
};

struct message_body {
    char            data[MAX_MESSAGE_SIZE];
};

struct message_item {
    struct message_head head;
    struct message_body body;
};

error_code message_init();

error_code message_open(struct message_queue *queue, enum queue queue_number);
error_code message_send(void *data, enum queue receive_queue);
error_code message_send_prio(void *data, enum queue receive_queue, enum queue_prio prio);
error_code message_receive(struct message_queue *queue, struct message_item* msg, int* len);
error_code message_get_queue(int *queue);

#endif // MESSAGES_H
