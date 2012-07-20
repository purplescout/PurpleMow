#ifndef SENSOR_BWF_H
#define SENSOR_BWF_H

#include "position.h"

struct bwf {
    int         x;
    int         y;
    int         width;
    int         height;
};

double sensor_bwf_get_distance(struct bwf* b, struct position* pos);
int sensor_bwf_get_value(double dist, int sensor);

#endif // SENSOR_BWF_H
