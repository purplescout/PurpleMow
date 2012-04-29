
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>  // exit
#include <string.h>
#include <sys/stat.h>

#include "messages.h"
#include "utils.h"

static void get_queue_name(enum queue queue_number, char* queue_name, int len);

error_code message_open(struct message_queue *this, enum queue queue_number)
{
    char queue_name[32];
    mqd_t queue;

    if ( this == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    get_queue_name(queue_number, queue_name, sizeof(queue_name));
    queue = mq_open(queue_name, O_CREAT | O_RDONLY, S_IRUSR | S_IWUSR, NULL);

    if ( queue == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to open queue: %d", queue);
        perror(error);
        exit(1);
    }

    this->queue = queue;
    snprintf(this->name, sizeof(this->name), "%s", queue_name);
    this->queue_number = queue_number;

    return err_OK;
}

error_code message_send(void *data, enum queue receive_queue)
{
    return message_send_prio(data, receive_queue, PRIO_NORMAL);
}

error_code message_send_prio(void *data, enum queue receive_queue, enum queue_prio prio)
{
    struct message_item *msg;
    char queue_name[32];
    mqd_t queue;
    mqd_t result;
    int length;

    msg = (struct message_item*)data;

    get_queue_name(receive_queue, queue_name, sizeof(queue_name));
    queue = mq_open(queue_name, O_WRONLY);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to open queue: %d", receive_queue);
        perror(error);
        exit(1);
    }

    result = mq_send(queue,
                     (char*)msg,
                     min(msg->head.length, sizeof(*msg)),
                     prio);

    result = mq_close(queue);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to close queue: %d", receive_queue);
        perror(error);
        exit(1);
    }

    return err_OK;
}

error_code message_receive(struct message_queue *this, struct message_item* msg, int* len)
{
    char* local_buffer;
    int local_len;
    struct message_item *local_msg;
    mqd_t result;
    struct mq_attr mq_attr;

    result = mq_getattr(this->queue, &mq_attr);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to get attr of queue: %d", this->queue_number);
        perror(error);
        exit(1);
    }

    local_len = mq_attr.mq_msgsize + 1;
    local_buffer = malloc(local_len);

    result = mq_receive(this->queue, local_buffer, local_len, NULL);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to receive from queue: %d", this->queue_number);
        perror(error);
        exit(1);
    }

    *len = min(*len, result);

    local_msg = (struct message_item*)local_buffer;

    if ( *len != local_msg->head.length )
        return err_MESSAGE;

    memcpy(msg, local_buffer, *len);

    return err_OK;
}

static void get_queue_name(enum queue queue_number, char* queue_name, int len)
{
    snprintf(queue_name, len, "/purplemow_%d", queue_number);
}

