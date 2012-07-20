#ifndef RANGE_TABLE_H
#define RANGE_TABLE_H

#include <string.h>

#include "table.h"

enum range_sensor {
    range_sensor_NONE = 0,
    range_sensor_GP2D120,
};

int range_data_GP2D120[41] = { 0, 2000, 2200, 3050, 2700,     // 0-4 cm
                               2300, 2000, 1750, 1550, 1400,  // 5-9 cm
                               1250, 1150, 1050, 1000, 930,   // 10-14 cm
                               875, 810, 770, 725, 690,       // 15-19 cm
                               650, 620, 600, 570, 535,       // 20-24 cm
                               505, 500, 445, 435, 425,       // 25-29 cm
                               415, 410, 405, 400, 390,       // 30-34 cm
                               375, 360, 345, 330, 315,       // 35-39 cm
                               300 };                         // 40 cm

struct table table_range_GP2D120 = {
    .size = sizeof(range_data_GP2D120)/sizeof(range_data_GP2D120[0]),
    .type = table_type_int,
    .factor = table_5000mV_in_10bits,
    .data.i = range_data_GP2D120 };



struct sensor range_sensors[] = {
    { "GP2D120", range_sensor_GP2D120, &table_range_GP2D120 },
    // anchor
    { "", range_sensor_NONE, NULL },
};


#endif // RANGE_TABLE_H
