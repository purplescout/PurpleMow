#ifndef DISTANCE_H
#define DISTANCE_H

struct point {
    double      x;
    double      y;
};

double distance(struct point* p, struct point* p1, struct point* p2);
double distance2(struct point* p, struct point* p1, struct point* p2);

#endif // DISTANCE_H
