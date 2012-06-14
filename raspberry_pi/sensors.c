
#include "error_codes.h"
#include "sensors.h"

#include "sensor_range.h"

/**
 * @defgroup sensor Sensor
 *
 * Sensors Group.
 *
 * @ingroup purplemow
 */

/**
 * @defgroup sensors Sensors
 * @ingroup sensor
 *
 * Handle to all sensors.
 */

/**
 * Sensors.
 *
 * @ingroup sensors
 */
struct sensors {
    struct sensor_range     range;
};

struct sensors this;

/**
 * Initialize sensors.
 *
 * @ingroup sensors
 * @return                  Success status.
 */
error_code sensors_init()
{
    error_code result;

    result = sensor_range_init(&this.range);

    if( FAILURE(result) )
        return result;

    return err_OK;
}

/**
 * Start the sensors.
 *
 * @ingroup sensors
 * @return                  Success status.
 */
error_code sensors_start()
{
    error_code result;

    result = sensor_range_start(&this.range);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}
