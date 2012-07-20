#ifndef SENSOR_OBSTACLE_H
#define SENSOR_OBSTACLE_H

#include "position.h"

struct obstacle {
    int         x;
    int         y;
    int         width;
    int         height;
};

double sensor_obstacle_get_distance(struct obstacle* obs, struct position* pos);
int sensor_obstacle_get_value(double dist, int sensor);

#endif // SENSOR_OBSTACLE_H
