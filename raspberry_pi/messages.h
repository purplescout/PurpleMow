#ifndef MESSAGES_H
#define MESSAGES_H

#include <mqueue.h>

#define MESSAGE_SIZE 64

enum queue
{
    Q_TEST,
};

enum queue_prio
{
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

struct message_item
{
    enum queue  queue_number;
    char        name[32];
    mqd_t       queue;
};

int message_open(struct message_item *this, enum queue queue_number);
int message_send(char *buffer, int len, enum queue receive_queue);
int message_receive(struct message_item *this, char* buffer, int* len);

#endif // MESSAGES_H
