
#include <math.h>

#include "distance.h"

static double length_squared(struct point* p1, struct point* p2);
static double dot(struct point* p1, struct point* p2, struct point* p3);
static void projection(struct point* proj, struct point* p1, struct point* p2, double tangent);

double distance(struct point* p, struct point* p1, struct point* p2) {
    return sqrt(distance2(p, p1, p2));
}

double distance2(struct point* p, struct point* p1, struct point* p2)
{
    struct point proj;
    double length2;
    double tangent;


    length2 = length_squared(p1, p2);

    if ( length2 == 0.0 ) {
        // p1 == p2
        return length_squared(p, p1);
    }

    tangent = dot(p, p1, p2);
    tangent = tangent / length2;

    if ( tangent < 0.0 ) {
        // Beyond p1
        return length_squared(p, p1);
    } else if ( tangent > 1.0 ) {
        // Beyond p2
        return length_squared(p, p2);
    }

    projection(&proj, p1, p2, tangent);

    return length_squared(p, &proj);
}

static double length_squared(struct point* p1, struct point* p2)
{
    double dx = p1->x - p2->x;
    double dy = p1->y - p2->y;

    return dx * dx + dy * dy;
}

// ( (p1-p2) · (p3-p2) )
// (   d1    ·   d2    )
//           dt
static double dot(struct point* p1, struct point* p2, struct point* p3)
{
    struct point d1;
    struct point d2;
    double dt;

    d1.x = p1->x - p2->x;
    d1.y = p1->y - p2->y;

    d2.x = p3->x - p2->x;
    d2.y = p3->y - p2->y;

    dt = d1.x * d2.x + d1.y * d2.y;

    return dt;
}

// p1 + tangent * (p2-p1)
static void projection(struct point* proj, struct point* p1, struct point* p2, double tangent)
{
    proj->x = p1->x + tangent * ( p2->x - p1->x );
    proj->y = p1->y + tangent * ( p2->y - p1->y );
}

