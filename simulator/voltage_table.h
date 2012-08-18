#ifndef VOLTAGE_TABLE_H
#define VOLTAGE_TABLE_H

#include <string.h>

#include "table.h"

enum voltage_sensor {
    voltage_sensor_NONE = 0,
    voltage_sensor_VOLTAGE,
};

// Linear between 12.0-10.0 V
int voltage_data_VOLTAGE[11] = { 12000, 11800, 11600, 11400, 11200,
                                 11000, 10800, 10600, 10400, 10200,
                                 10000 };


struct table table_voltage_VOLTAGE = {
    .size = sizeof(voltage_data_VOLTAGE)/sizeof(voltage_data_VOLTAGE[0]),
    .type = table_type_int,
    .factor = table_12000mV_in_10bits,
    .data.i = voltage_data_VOLTAGE };

// First sensor in this table is the default in the simulator
struct sensor voltage_sensors[] = {
    { "VOLTAGE", voltage_sensor_VOLTAGE, &table_voltage_VOLTAGE },
    // anchor
    { "", voltage_sensor_NONE, NULL },
};

#endif // VOLTAGE_TABLE_H
