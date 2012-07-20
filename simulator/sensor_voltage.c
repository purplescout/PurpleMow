
#include "sensor_voltage.h"

#include "voltage_table.h"

int sensor_voltage_get_value(int voltage, int sensor)
{
    int value;
    int i;
    struct table* table;

    i = ( 12000 - voltage ) / 200;
    table = table_get_table(sensor, voltage_sensors);

    value = table_lookup(table, i) * table->factor;

    return value;
}

