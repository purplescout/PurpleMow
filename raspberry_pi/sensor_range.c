#include <string.h>

#include "purplemow.h"
#include "thread.h"
#include "error_codes.h"
#include "communicator.h"
#include "messages.h"
#include "poller.h"

#define POLL_INTERVAL   500000

#define RANGE_TOO_CLOSE 380

/**
 * @defgroup sensor Sensor
 *
 * Sensors Group.
 *
 * @ingroup purplemow
 */

/**
 * @defgroup sensor_range
 * @ingroup sensor
 *
 * Range sensor Group.
 */

/**
 * @ingroup sensor_range
 */
struct sensor_range {
    struct message_queue    message_handle;
    pthread_t               thread;
    struct poller           poller;
};

// Thread
static void* sensor_range_worker(void *data);

// Poller
static error_code sensor_range_poll(void *data);

// Private functions
static error_code handle_range_sensor(enum sensor sensor, int value);

static struct sensor_range this;

/**
 * Initialize sensor_range.
 *
 * @ingroup sensor_range
 * @return                  Success status.
 */
error_code sensor_range_init()
{
    error_code result;

    result = message_open(&this.message_handle, Q_SENSOR_RANGE);

    if ( FAILURE(result) )
        return result;

    result = poller_create(&this.poller, poller_sleep_usec, POLL_INTERVAL, sensor_range_poll, NULL);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}

/**
 * Start the sensor_range.
 *
 * @ingroup sensor_range
 * @return                  Success status.
 */
error_code sensor_range_start()
{
    error_code result;

    result = thread_start(&this.thread, sensor_range_worker);

    if ( FAILURE(result) )
        return result;

    result = poller_start(&this.poller);

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
    struct message_item         msg_buff;
    struct message_sensor_data  *msg;
    int len;
    error_code result;

    while ( 1 ) {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this.message_handle, &msg_buff, &len);

        if ( SUCCESS(result) ) {
            msg = (struct message_sensor_data*)&msg_buff;
            switch (msg->head.type) {
                case MSG_SENSOR_DATA:
                    handle_range_sensor(msg->body.sensor, msg->body.value);
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
    communicator_read(sensor_range, Q_SENSOR_RANGE);

    return err_OK;
}

/**
 * Handle the raw value read from the range sensor.
 *
 * @param[in] sensor        The sensor
 * @param[in] value         Read value
 *
 * @return                  Success status
 */
static error_code handle_range_sensor(enum sensor sensor, int value)
{
    static int old_value = RANGE_TOO_CLOSE;

    switch (sensor) {
        case sensor_range:
            break;
        default:
            return err_UNHANDLED_SENSOR;
    }

    if ( old_value < RANGE_TOO_CLOSE && value >= RANGE_TOO_CLOSE ) {
        main_range_too_close();
    } else if ( old_value >= RANGE_TOO_CLOSE && value < RANGE_TOO_CLOSE ) {
        main_range_ok();
    }

    old_value = value;

    return err_OK;
}
