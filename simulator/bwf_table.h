#ifndef BWF_TABLE_H
#define BWF_TABLE_H

#include <string.h>

#include "table.h"

enum bwf_sensor {
    bwf_sensor_NONE = 0,
    bwf_sensor_BWF,
    bwf_sensor_BWF_Inv,
};

// Theoretical values from function 4000+sqrt(x)*223.6
int bwf_data_BWF[21] = { 4000, 4224, 4316, 4387, 4447,  // 0-4 cm
                         4500, 4548, 4592, 4632, 4671,  // 5-9 cm
                         4707, 4742, 4775, 4806, 4837,  // 10-14 cm
                         4866, 4894, 4922, 4949, 4975,  // 15-19 cm
                         5000 };                        // 20 cm

// Theoretical values from function 4000-sqrt(x)*365
int bwf_data_BWF_Inv[31] = { 4000, 3635, 3484, 3368, 3270, // 0-4 cm
                             3184, 3106, 3035, 2968, 2905, // 5-9 cm
                             2846, 2790, 2736, 2684, 2635, // 10-14 cm
                             2587, 2540, 2495, 2452, 2409, // 15-19 cm
                             2368, 2328, 2288, 2250, 2212, // 20-24 cm
                             2175, 2139, 2103, 2069, 2034, // 25-29 cm
                             2001 };                       // 30 cm

struct table table_bwf_BWF = {
    .size = sizeof(bwf_data_BWF)/sizeof(bwf_data_BWF[0]),
    .type = table_type_int,
    .factor = table_5000mV_in_10bits,
    .data.i = bwf_data_BWF };

struct table table_bwf_BWF_Inv = {
    .size = sizeof(bwf_data_BWF_Inv)/sizeof(bwf_data_BWF_Inv[0]),
    .type = table_type_int,
    .factor = table_5000mV_in_10bits,
    .data.i = bwf_data_BWF_Inv };

// First sensor in this table is the default in the simulator
struct sensor bwf_sensors[] = {
    { "BWF", bwf_sensor_BWF, &table_bwf_BWF },
    { "BWF Inv", bwf_sensor_BWF_Inv, &table_bwf_BWF_Inv },
    // anchor
    { "", bwf_sensor_NONE, NULL },
};

#endif // BWF_TABLE_H
