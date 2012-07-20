
#include "sensor_moisture.h"

#include "moisture_table.h"

int sensor_moisture_get_value(int moisture, int sensor)
{
    int value;
    int d;
    struct table* table;

    d = (moisture + 2)/5;
    table = table_get_table(sensor, moisture_sensors);

    value = table_lookup(table, d) * table->factor;

    return value;
}

