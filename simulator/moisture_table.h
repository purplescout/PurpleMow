#ifndef MOISTURE_TABLE_H
#define MOISTURE_TABLE_H

#include <string.h>

#include "table.h"

enum moisture_sensor {
    moisture_sensor_NONE = 0,
    moisture_sensor_MOISTURE,
};

// A linear sensor 0-100 %
int moisture_data_MOISTURE[21] = { 0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
                                   55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };


struct table table_moisture_MOISTURE = {
    .size = sizeof(moisture_data_MOISTURE)/sizeof(moisture_data_MOISTURE[0]),
    .type = table_type_int,
    .factor = table_100_percent_in_10bits,
    .data.i = moisture_data_MOISTURE };

// First sensor in this table is the default in the simulator
struct sensor moisture_sensors[] = {
    { "MOISTURE", moisture_sensor_MOISTURE, &table_moisture_MOISTURE },
    // anchor
    { "", moisture_sensor_NONE, NULL },
};

#endif // MOISTURE_TABLE_H
