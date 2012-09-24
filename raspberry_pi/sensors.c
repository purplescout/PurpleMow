
#include "error_codes.h"
#include "modules.h"
#include "sensors.h"

#include "sensor_range.h"
#include "sensor_bwf.h"

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

// Private functions
static error_code sensors_start(void* data);

/**
 * Sensors.
 *
 * @ingroup sensors
 */
struct sensors {
    struct sensor_range     range;
    struct sensor_bwf       bwf;
};

static struct sensors this;

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
    result = sensor_bwf_init(&this.bwf);

    if( FAILURE(result) )
        return result;

    module_register_to_phase(phase_START_SENSORS, sensors_start, NULL);

    return err_OK;
}

/**
 * Start the sensors.
 *
 * @ingroup sensors
 * @return                  Success status.
 */
static error_code sensors_start(void* data)
{
    error_code result;

    result = sensor_range_start(&this.range);
    result = sensor_bwf_start(&this.bwf);

    if ( FAILURE(result) )
        return result;

    return err_OK;
}
