#include <string.h>

#include "purplemow.h"
#include "thread.h"
#include "error_codes.h"
#include "communicator.h"
#include "messages.h"
#include "poller.h"
#include "sensor_range.h"

#define POLL_INTERVAL           500000

#define RANGE_TOO_CLOSE         380
#define RANGE_HYSTERESIS        100

#define RANGE_STATE_TOO_CLOSE   1
#define RANGE_STATE_OK          0

/**
 * @defgroup sensor_range
 * @ingroup sensor
 *
 * Range sensor Group.
 */

// Thread
static void* sensor_range_worker(void *data);

// Poller
static error_code sensor_range_poll(void *data);

// Private functions
static error_code handle_range_sensor(struct sensor_range* this, enum sensor sensor, int value);

static struct sensor_range this;

/**
 * Initialize sensor_range.
 *
 * @ingroup sensor_range
 *
 * @param[in] this          The sensor_range
 *
 * @return                  Success status.
 */
error_code sensor_range_init(struct sensor_range* this)
{
    error_code result;

    result = message_get_queue(&this->queue);

    if ( FAILURE(result) )
        return result;

    result = message_open(&this->message_handle, this->queue);

    if ( FAILURE(result) )
        return result;

    result = poller_create(&this->poller, poller_sleep_usec, POLL_INTERVAL, sensor_range_poll, this);

    if ( FAILURE(result) )
        return result;

    this->state = RANGE_STATE_TOO_CLOSE;

    return err_OK;
}

/**
 * Start the sensor_range.
 *
 * @ingroup sensor_range
 *
 * @param[in] this          The sensor_range
 *
 * @return                  Success status.
 */
error_code sensor_range_start(struct sensor_range* this)
{
    error_code result;

    result = thread_start_data(&this->thread, sensor_range_worker, this);

    if ( FAILURE(result) )
        return result;

    result = poller_start(&this->poller);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}

/**
 * Handles incoming messages.
 *
 * @ingroup sensor_range
 *
 * @param[in] data  Data to the thread.
 *
 * @return          Return value from thread.
 */
static void* sensor_range_worker(void *data)
{
    struct sensor_range*        this;
    struct message_item         msg_buff;
    struct message_sensor_data  *msg;
    int len;
    error_code result;

    this = (struct sensor_range*)data;

    while ( 1 ) {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this->message_handle, &msg_buff, &len);

        if ( SUCCESS(result) ) {
            msg = (struct message_sensor_data*)&msg_buff;
            switch (msg->head.type) {
                case MSG_SENSOR_DATA:
                    handle_range_sensor(this, msg->body.sensor, msg->body.value);
                    break;
            }
        }
    }
}

/**
 * Handler for poller. Sends a read request on range sensor.
 *
 * @ingroup sensor_range
 *
 * @param[in] data          Data from the poller
 *
 * @return                  Success status
 */
static error_code sensor_range_poll(void *data)
{
    struct sensor_range*        this;

    this = (struct sensor_range*)data;

    communicator_read(sensor_range, this->queue);

    return err_OK;
}

/**
 * Handle the raw value read from the range sensor.
 *
 * @ingroup sensor_range
 *
 * @param[in] sensor        The sensor
 * @param[in] value         Read value
 *
 * @return                  Success status
 */
static error_code handle_range_sensor(struct sensor_range* this, enum sensor sensor, int value)
{

    switch (sensor) {
        case sensor_range:
            break;
        default:
            return err_UNHANDLED_SENSOR;
    }

#if 0
    if ( old_value < RANGE_TOO_CLOSE && value >= RANGE_TOO_CLOSE ) {
        main_range_too_close();
    } else if ( old_value >= RANGE_TOO_CLOSE && value < RANGE_TOO_CLOSE ) {
        main_range_ok();
    }
#endif

    switch ( this->state ) {
        case RANGE_STATE_TOO_CLOSE:
            if ( value < RANGE_TOO_CLOSE - RANGE_HYSTERESIS ) {
                main_range_ok();
                this->state = RANGE_STATE_OK;
            }
            break;
        case RANGE_STATE_OK:
            if ( value >= RANGE_TOO_CLOSE ) {
                main_range_too_close();
                this->state = RANGE_STATE_TOO_CLOSE;
            }
            break;
    }

    return err_OK;
}
