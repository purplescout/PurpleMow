
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>  // exit
#include <string.h>
#include <sys/stat.h>

#include "messages.h"
#include "utils/utils.h"

/**
 * @defgroup message Message
 * Message. Send and receive messages between modules.
 *
 * @ingroup purplemow
 */

/**
 * Message.
 *
 * @ingroup message
 */
struct message {
    int         dynamic_queues;
};

// Private functions
static void get_queue_name(enum queue queue_number, char* queue_name, int len);

struct message this;

/**
 * Initialize messages
 *
 * @ingroup message
 *
 * @return          Success status
 */
error_code message_init()
{
    this.dynamic_queues = 0;

    return err_OK;
}

/**
 * Open a message queue.
 *
 * @ingroup message
 *
 * @param[in] queue         Pointer to message_queue to use
 * @param[in] queue_number  Queue number to open
 *
 * @return                  Success status
 */
error_code message_open(struct message_queue *queue, enum queue queue_number)
{
    char queue_name[32];
    mqd_t mqueue;

    if ( queue == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    get_queue_name(queue_number, queue_name, sizeof(queue_name));
    mqueue = mq_open(queue_name, O_CREAT | O_RDONLY, S_IRUSR | S_IWUSR, NULL);

    if ( mqueue == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to open queue: %d", mqueue);
        perror(error);
        exit(1);
    }

    queue->queue = mqueue;
    snprintf(queue->name, sizeof(queue->name), "%s", queue_name);
    queue->queue_number = queue_number;

    return err_OK;
}

/**
 * Send a message to a queue with normal priority.
 *
 * @ingroup message
 *
 * @param[in] data              Message to send
 * @param[in] receive_queue     Queue to send message to
 *
 * @return                      Success status
 */
error_code message_send(void *data, enum queue receive_queue)
{
    return message_send_prio(data, receive_queue, PRIO_NORMAL);
}

/**
 * Send a message to a queue with selected priority.
 *
 * @ingroup message
 *
 * @param[in] data              Message to send
 * @param[in] receive_queue     Queue to send message to
 * @param[in] prio              Priority to send message with
 *
 * @return                      Success status
 */
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

/**
 * Receive a message, blocks until a message is received.
 *
 * @ingroup message
 *
 * @param[in]  queue    Pointer to message_queue to use
 * @param[out] msg      Buffer to write received message to
 * @param[out] len      Length of received message
 *
 * @return              Success status
 */
error_code message_receive(struct message_queue *queue, struct message_item* msg, int* len)
{
    char* local_buffer;
    int local_len;
    struct message_item *local_msg;
    mqd_t result;
    struct mq_attr mq_attr;

    result = mq_getattr(queue->queue, &mq_attr);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to get attr of queue: %d", queue->queue_number);
        perror(error);
        exit(1);
    }

    local_len = mq_attr.mq_msgsize + 1;
    local_buffer = malloc(local_len);

    result = mq_receive(queue->queue, local_buffer, local_len, NULL);

    if ( result == -1 )
    {
        char error[32];
        snprintf(error, sizeof(error), "Failed to receive from queue: %d", queue->queue_number);
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

/**
 * Get the string name of a queue.
 *
 * @ingroup message
 *
 * @param[in]  queue_number     Queue number
 * @param[out] queue_name       Buffer to write queue name to
 * @param[in]  len              Length of buffer
 */
static void get_queue_name(enum queue queue_number, char* queue_name, int len)
{
    snprintf(queue_name, len, "/purplemow_%d", queue_number);
}

/**
 * Get a dynamic queue number.
 *
 * @ingroup message
 *
 * @param[out] queue        Returned queue number
 *
 * @return                  Success status
 */
error_code message_get_queue(int *queue)
{
    *queue = this.dynamic_queues + Q_LAST;
    this.dynamic_queues++;

    return err_OK;
}

