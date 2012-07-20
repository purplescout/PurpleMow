
#include <math.h>

#include "sensor_bwf.h"
#include "distance.h"

#include "bwf_table.h"

#define min(x,y)    (x) < (y) ? (x) : (y)

#ifdef DEBUG
#include <stdio.h>
int main()
{
    return 0;
}
#endif // DEBUG

double sensor_bwf_get_distance(struct bwf* b, struct position* pos)
{
    struct point p;
    struct point p1;
    struct point p2;

    double dist;

    // width > height
    //
    //   x,y      width
    //    +------------------+
    //    |                  | height
    // ---p1-----------------p2--  line
    //    |                  |
    //    +------------------+
    //
    // height > width
    //
    //   x,y | width
    //    +--p1-+
    //    |  |  |
    //    |  |  | height
    //    |  |  |
    //    +--p2-+
    //       |
    //       line

    p.x = pos->x;
    p.y = pos->y;

    if ( b->width > b->height ) {
        p1.x = b->x;
        p1.y = b->y + b->height/2;

        p2.x = b->x + b->width;
        p2.y = b->y + b->height/2;
    } else {
        p1.x = b->x + b->width/2;
        p1.y = b->y;

        p2.x = b->x + b->width/2;
        p2.y = b->y + b->height;
    }

    dist = distance2(&p, &p1, &p2);
    dist = sqrt(dist);

    return dist;
}

int sensor_bwf_get_value(double dist, int sensor)
{
    int value;
    int d;
    struct table* table;

    d = (int)(dist + 0.5);
    table = table_get_table(sensor, bwf_sensors);

    value = table_lookup(table, d) * table->factor;

    return value;
}

