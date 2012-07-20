
#include <math.h>

#include "sensor_obstacle.h"
#include "distance.h"

#include "table.h"
#include "range_table.h"

#define min(x,y)    (x) < (y) ? (x) : (y)

#ifdef DEBUG
#include <stdio.h>
int main()
{
    struct obstacle o1;
    struct position p1;

    int dist;

    o1.x = 10;
    o1.y = 10;
    o1.width = 10;
    o1.height = 10;

    p1.x = 5;
    p1.y = 5;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 10;
    p1.y = 10;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 25;
    p1.y = 25;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 27;
    p1.y = 27;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 30;
    p1.y = 30;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 100;
    p1.y = 100;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    p1.x = 1000;
    p1.y = 1000;

    dist = sensor_obstacle_get_value(&o1, &p1);

    printf("%d,%d distance: %d\n", p1.x, p1.y, dist);

    return 0;
}
#endif // DEBUG

double sensor_obstacle_get_distance(struct obstacle* obs, struct position* pos)
{
    double d1;
    double d2;
    double d3;
    double d4;

    struct point p1;
    struct point p2;
    struct point p3;
    struct point p4;

    struct point p;

    double m1;
    double m2;

    double dist;

    p.x = pos->x;
    p.y = pos->y;

    // p = point
    // l = line segment
    // d = distance from line segment

    //        l1
    //    p1------p2          p
    //    |        |
    // l4 |        | l2
    //    |        |
    //    p4------p3
    //        l3

    p1.x = obs->x;
    p1.y = obs->y;

    p2.x = obs->x + obs->width;
    p2.y = obs->y;

    p3.x = obs->x + obs->width;
    p3.y = obs->y + obs->height;

    p4.x = obs->x;
    p4.y = obs->y + obs->height;

    d1 = distance2(&p, &p1, &p2);
    d2 = distance2(&p, &p2, &p3);
    d3 = distance2(&p, &p3, &p4);
    d4 = distance2(&p, &p4, &p1);

    m1 = min(d1, d2);
    m2 = min(d3, d4);
    dist = min(m1, m2);

    dist = sqrt(dist);

    return dist;
}

int sensor_obstacle_get_value(double dist, int sensor)
{
    int value;
    int d;
    struct table* table;

    d = (int)(dist + 0.5);
    table = table_get_table(sensor, range_sensors);

    value = table_lookup(table, d) * table->factor;
#ifdef DEBUG
    printf("distance: %f, distance: %d -> value: %d\n", dist, d, value);
#endif // DEBUG
    return value;
}

