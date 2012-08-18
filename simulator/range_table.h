#ifndef RANGE_TABLE_H
#define RANGE_TABLE_H

#include <string.h>

#include "table.h"

enum range_sensor {
    range_sensor_NONE = 0,
    range_sensor_GP2D120,
    range_sensor_GP2Y0A21YK,
};

// GP2D120
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

// GP2Y0A21YK
int range_data_GP2Y0A21YK[81] = { 0, 750, 1300, 2000, 2650,     // 0-4 cm
                                  3100, 3150, 2950, 2700, 2500, // 5-9 cm
                                  2275, 2150, 2005, 1900, 1775, // 10-14 cm
                                  1650, 1575, 1500, 1425, 1375, // 15-19 cm
                                  1300, 1250, 1225, 1175, 1125, // 20-24 cm
                                  1075, 1025, 1000, 975, 950,   // 25-29 cm
                                  925, 900, 875, 850, 835,      // 30-34 cm
                                  825, 800, 775, 760, 745,      // 35-39 cm
                                  725, 710, 695, 675, 665,      // 40-44 cm
                                  655, 645, 630, 615, 600,      // 45-49 cm
                                  590, 580, 570, 560, 550,      // 50-54 cm
                                  545, 540, 530, 520, 510,      // 55-59 cm
                                  500, 490, 485, 480, 475,      // 60-64 cm
                                  470, 460, 455, 450, 445,      // 65-69 cm
                                  440, 435, 430, 425, 420,      // 70-74 cm
                                  415, 410, 407, 404, 402,      // 75-79 cm
                                  400 };                        // 80 cm

struct table table_range_GP2Y0A21YK = {
    .size = sizeof(range_data_GP2Y0A21YK)/sizeof(range_data_GP2Y0A21YK[0]),
    .type = table_type_int,
    .factor = table_5000mV_in_10bits,
    .data.i = range_data_GP2Y0A21YK };

// First sensor in this table is the default in the simulator
struct sensor range_sensors[] = {
    { "GP2Y0A21YK", range_sensor_GP2Y0A21YK, &table_range_GP2Y0A21YK },
    { "GP2D120", range_sensor_GP2D120, &table_range_GP2D120 },
    // anchor
    { "", range_sensor_NONE, NULL },
};


#endif // RANGE_TABLE_H
