#include <string.h>
#include <stdio.h>

#include "purplemow.h"
#include "thread.h"
#include "error_codes.h"
#include "communicator.h"
#include "messages.h"
#include "modules.h"
#include "poller.h"
#include "sensor_bwf.h"
#include "mow.h"

#define POLL_INTERVAL           500000

#define BWF_TOO_CLOSE         902
#define BWF_HYSTERESIS        42

#define BWF_STATE_TOO_CLOSE   1
#define BWF_STATE_OK          0

/**
 * @defgroup sensor_bwf
 * @ingroup sensor
 *
 * BWF sensor Group.
 */

// Thread
static void* sensor_bwf_worker(void *data);

// Poller
static error_code sensor_bwf_poll(void *data);

// Private functions
static error_code handle_bwf_sensor(struct sensor_bwf* this, enum sensor sensor, int value);

static struct sensor_bwf this;

/**
 * Initialize sensor_bwf.
 *
 * @ingroup sensor_bwf
 *
 * @param[in] this          The sensor_bwf
 *
 * @return                  Success status.
 */
error_code sensor_bwf_init(struct sensor_bwf* this)
{
    error_code result;

    result = message_get_queue(&this->queue);

    if ( FAILURE(result) )
        return result;

    result = message_open(&this->message_handle, this->queue);

    if ( FAILURE(result) )
        return result;

    result = poller_create(&this->poller, poller_sleep_usec, POLL_INTERVAL, sensor_bwf_poll, this);

    if ( FAILURE(result) )
        return result;

    this->state = BWF_STATE_TOO_CLOSE;

    return err_OK;
}

/**
 * Start the sensor_bwf.
 *
 * @ingroup sensor_bwf
 *
 * @param[in] this          The sensor_bwf
 *
 * @return                  Success status.
 */
error_code sensor_bwf_start(struct sensor_bwf* this)
{
    error_code result;

    result = thread_start_data(&this->thread, sensor_bwf_worker, this);

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
 * @ingroup sensor_bwf
 *
 * @param[in] data  Data to the thread.
 *
 * @return          Return value from thread.
 */
static void* sensor_bwf_worker(void *data)
{
    struct sensor_bwf*        this;
    struct message_item         msg_buff;
    struct message_sensor_data  *msg;
    int len;
    error_code result;

    this = (struct sensor_bwf*)data;

    while ( 1 ) {
        memset(&msg_buff, 0, sizeof(msg_buff) );
        len = sizeof(msg_buff);
        result = message_receive(&this->message_handle, &msg_buff, &len);

        if ( SUCCESS(result) ) {
            msg = (struct message_sensor_data*)&msg_buff;
            switch (msg->head.type) {
                case MSG_SENSOR_DATA:
                    handle_bwf_sensor(this, msg->body.sensor, msg->body.value);
                    break;
            }
        }
    }
}

/**
 * Handler for poller. Sends a read request on bwf sensor.
 *
 * @ingroup sensor_bwf
 *
 * @param[in] data          Data from the poller
 *
 * @return                  Success status
 */
static error_code sensor_bwf_poll(void *data)
{
    struct sensor_bwf*        this;

    this = (struct sensor_bwf*)data;

    communicator_read(sensor_bwf_left, this->queue);

    return err_OK;
}

/**
 * Handle the raw value read from the bwf sensor.
 *
 * @ingroup sensor_bwf
 *
 * @param[in] sensor        The sensor
 * @param[in] value         Read value
 *
 * @return                  Success status
 */
static error_code handle_bwf_sensor(struct sensor_bwf* this, enum sensor sensor, int value)
{

    switch (sensor) {
        case sensor_bwf_left:
            break;
        default:
            return err_UNHANDLED_SENSOR;
    }

    switch ( this->state ) {
        case BWF_STATE_TOO_CLOSE:
            if ( value > BWF_TOO_CLOSE + BWF_HYSTERESIS ) {
                mow_bwf(sensor_bwf_left, decision_bwf_ok);
                this->state = BWF_STATE_OK;
            }
            break;
        case BWF_STATE_OK:
            if ( value <= BWF_TOO_CLOSE ) {
                mow_bwf(sensor_bwf_left, decision_bwf_too_close);
                this->state = BWF_STATE_TOO_CLOSE;
            }
            break;
    }

    printf("bwf: value: %d, state: %d\n", value, this->state );

    return err_OK;
}
