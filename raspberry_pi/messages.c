
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>  // exit
#include <string.h>
#include <sys/stat.h>

#include "messages.h"
#include "utils.h"

static void get_queue_name(enum queue queue_number, char* queue_name, int len);

error_code message_open(struct message_item *this, enum queue queue_number)
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

error_code message_send(char *buffer, int len, enum queue receive_queue)
{
    char queue_name[32];
    mqd_t queue;
    mqd_t result;

    if ( len == 0 )
    {
        return err_OK;
    }

    get_queue_name(receive_queue, queue_name, sizeof(queue_name));
    queue = mq_open(queue_name, O_WRONLY);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to open queue: %d", receive_queue);
        perror(error);
        exit(1);
    }

    result = mq_send(queue, buffer, min(len, MESSAGE_SIZE), PRIO_NORMAL);

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

error_code message_receive(struct message_item *this, char* buffer, int* len)
{
    char* local_buffer;
    int local_len;
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

    memcpy(buffer, local_buffer, *len);

    return err_OK;
}

static void get_queue_name(enum queue queue_number, char* queue_name, int len)
{
    snprintf(queue_name, len, "/purplemow_%d", queue_number);
}

