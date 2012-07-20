
// build:
// gcc -ggdb -DDEBUG_PRINT -DDEBUG -o test mower_calc.c -lm && ./test | grep ^m > data.dat
// gcc -ggdb -DDEBUG_PRINT -DDEBUG -o test mower_calc.c -lm && ./test

#include <math.h>
#include <stdio.h>

#include "position.h"

#include "mower_calc.h"

static void fast_turn(struct mower_pos* m, double dist1, double dist2);
static void normal_turn(struct mower_pos* m, double dist1, double dist2);

static double get_radius_between(double dist1, double dist2, double offset);
static double get_radius(double dist1, double dist2, double offset);
static double get_new_angle(double dist, double r);
static double get_old_angle(struct position p1, struct position p2);
static struct position get_position(double r, double angle);
static double radians_to_degree(double rad);

#ifdef DEBUG

#define ITERATIONS 15
static int debug = 1000000;
static void run(double x1, double y1, double x2, double y2, double d1, double d2, double o, double d, int it);

int main()
{
    double step_size = 5.0;
    debug = 0;

    // normal turns
    run(250.0, 250.0, 300.0, 250.0,   step_size, step_size*2.0,   50.0,   1.0 * pi / 2.0,   ITERATIONS);
    run(250.0, 250.0, 300.0, 250.0,   step_size*2.0, step_size,   50.0,   1.0 * pi / 2.0,   ITERATIONS);

    run(300.0, 250.0, 250.0, 250.0,   step_size, step_size*2.0,   50.0,   3.0 * pi / 2.0,   ITERATIONS);
    run(300.0, 250.0, 250.0, 250.0,   step_size*2.0, step_size,   50.0,   3.0 * pi / 2.0,   ITERATIONS);

    run(250.0, -50.0, 300.0, -50.0,   0.0, step_size,    50.0,   1.0 * pi / 2.0,   ITERATIONS);
    run(250.0, -50.0, 300.0, -50.0,   step_size, 0.0,    50.0,   1.0 * pi / 2.0,   ITERATIONS);

    run(300.0, -50.0, 250.0, -50.0,   0.0, step_size,    50.0,   3.0 * pi / 2.0,   ITERATIONS);
    run(300.0, -50.0, 250.0, -50.0,   step_size, 0.0,    50.0,   3.0 * pi / 2.0,   ITERATIONS);


    run(-250.0, 250.0, -250.0, 300.0,   step_size, step_size*2.0,   50.0,   2.0 * pi / 2.0,   ITERATIONS);
    run(-250.0, 250.0, -250.0, 300.0,   step_size*2.0, step_size,   50.0,   2.0 * pi / 2.0,   ITERATIONS);

    run(-250.0, 300.0, -250.0, 250.0,   step_size, step_size*2.0,   50.0,   4.0 * pi / 2.0,   ITERATIONS);
    run(-250.0, 300.0, -250.0, 250.0,   step_size*2.0, step_size,   50.0,   4.0 * pi / 2.0,   ITERATIONS);

    run(-250.0, 650.0, -250.0, 700.0,   0.0, step_size,   50.0,   2.0 * pi / 2.0,   ITERATIONS);
    run(-250.0, 650.0, -250.0, 700.0,   step_size, 0.0,   50.0,   2.0 * pi / 2.0,   ITERATIONS);

    run(-250.0, 700.0, -250.0, 650.0,   0.0, step_size,   50.0,   4.0 * pi / 2.0,   ITERATIONS);
    run(-250.0, 700.0, -250.0, 650.0,   step_size, 0.0,   50.0,   4.0 * pi / 2.0,   ITERATIONS);

    // fast turns
    run(100.0, 150.0, 150.0, 150.0,   step_size, -step_size,   50.0,   1.0 * pi / 2.0,   ITERATIONS / 2);
    run(100.0, 250.0, 150.0, 250.0,   -step_size, step_size,   50.0,   1.0 * pi / 2.0,   ITERATIONS / 2);

    run(100.0, 450.0, 150.0, 450.0,   step_size*2.0, -step_size,    50.0,   1.0 * pi / 2.0,   ITERATIONS / 2);
    run(100.0, 350.0, 150.0, 350.0,   -step_size*2.0, step_size,    50.0,   1.0 * pi / 2.0,   ITERATIONS / 2);

    run(-50.0, 100.0, -50.0, 150.0,   step_size, -step_size,   50.0,   2.0 * pi / 2.0,   ITERATIONS / 2);
    run(-50.0, 200.0, -50.0, 250.0,   -step_size, step_size,   50.0,   2.0 * pi / 2.0,   ITERATIONS / 2);

    run(-50.0, 300.0, -50.0, 350.0,   step_size*2.0, -step_size,   50.0,   2.0 * pi / 2.0,   ITERATIONS / 2);
    run(-50.0, 400.0, -50.0, 450.0,   -step_size*2.0, step_size,    50.0,   2.0 * pi / 2.0,   ITERATIONS / 2);


    return 0;
}

static void run(double x1, double y1, double x2, double y2, double d1, double d2, double o, double d, int it)
{
    struct mower_pos m;

    double dist1;
    double dist2;

    int i = 0;

    m.left.x = x1;
    m.left.y = y1;
    m.right.x = x2;
    m.right.y = y2;

    m.direction = d;
    m.offset = o;

    // distance travelled
    // turn left
    dist1 = d1;
    dist2 = d2;

    printf("m1: %f %f\n", m.left.x, m.left.y);
    printf("m2: %f %f\n", m.right.x, m.right.y);

    i = 0;
    while ( i++ < it ) {
#ifdef DEBUG_PRINT
        printf("---------------\n");
#endif // DEBUG_PRINT
        update_positions(&m, dist1, dist2);

        printf("m1: %f %f\n", m.left.x, m.left.y);
        printf("m2: %f %f\n", m.right.x, m.right.y);

    }

#ifdef DEBUG_PRINT
    printf("===============\n");
#endif // DEBUG_PRINT

    return;
}
#endif // DEBUG

void init_mower_pos(struct mower_pos* p, double offset)
{
    p->left.x = 750.0;
    p->left.y = 750.0 + offset;
    p->right.x = 750.0;
    p->right.y = 750.0;
    p->offset = offset;
    p->direction = 0;
}

void update_positions(struct mower_pos* m, double dist1, double dist2)
{
    // TODO: add support for fast turns, ie one motor forward and one motor backward
    if ( dist1 > 0.0 && dist2 < 0.0 ||
         dist1 < 0.0 && dist2 > 0.0 )
        fast_turn(m, dist1, dist2);
    else
        normal_turn(m, dist1, dist2);
}

static void fast_turn(struct mower_pos* m, double dist1, double dist2)
{
    // offset between world coordinates and new coordinates
    struct position o1;
    struct position o2;

    // start position, ie current position
    struct position s1;
    struct position s2;

    double r1;
    double r2;

    double a1;
    double a2;
    double a;

    r1 = get_radius_between(dist1, -dist2, m->offset);
    r2 = get_radius_between(-dist2, dist1, m->offset);

    if ( r1 > 0.0 )
        a1 = get_new_angle(dist1, r1);
    else
        a1 = get_new_angle(dist2, r2);

    a2 = m->direction - pi/2.0;
    a = a1+a2;
    m->direction += a1;

    // Get start position
    s1 = get_position(r1, a2);
    s2 = get_position(r2, a2);

    o1 = get_position(r1, a);
    o2 = get_position(r2, a);

    m->left.x -= ( o1.x - s1.x );
    m->left.y -= ( o1.y - s1.y );
    m->right.x += o2.x - s2.x;
    m->right.y += o2.y - s2.y;

#ifdef DEBUG_PRINT
    printf("d1: %f\n", dist1);
    printf("d2: %f\n", dist2);
    printf("r1: %f\n", r1);
    printf("r2: %f\n", r2);
    printf("a1: %f\n", a1);
    printf("a1: %f\n", radians_to_degree(a1));
    printf("a2: %f\n", a2);
    printf("a2: %f\n", radians_to_degree(a2));
    printf("a: %f\n", a);
    printf("a: %f\n", radians_to_degree(a));
    printf("o1: %f %f\n", o1.x, o1.y);
    printf("o2: %f %f\n", o2.x, o2.y);
    printf("s1: %f %f\n", s1.x, s1.y);
    printf("s2: %f %f\n", s2.x, s2.y);
#endif // DEBUG_PRINT


    while ( m->direction > 2.0 * pi )
        m->direction -= 2.0 * pi;

    while ( m->direction < 0 )
        m->direction += 2.0 * pi;
}

static void normal_turn(struct mower_pos* m, double dist1, double dist2)
{
    // offset between world coordinates and new coordinates
    struct position o1;
    struct position o2;

    // start position, ie current position
    struct position s1;
    struct position s2;

    double r1;
    double r2;

    struct position p;
    double a1;
    double a2;
    double a;

    int clockwise = 0;

    if ( dist1 == dist2 ) {
        m->left.x += dist1 * cos(m->direction);
        m->left.y += dist1 * sin(m->direction);
        m->right.x += dist2 * cos(m->direction);
        m->right.y += dist2 * sin(m->direction);
        return;
    }

    if ( dist2 < dist1 && dist1 > 0.0 && dist2 > 0.0 ||
         dist2 > dist1 && dist1 < 0.0 && dist2 < 0.0 ) {
        clockwise = 1;
    }

    if ( clockwise ) {
        struct position tmp_p;
        double tmp_d;
        tmp_d = -dist1;
        dist1 = -dist2;
        dist2 = tmp_d;
        tmp_p = m->left;
        m->left = m->right;
        m->right = tmp_p;
        m->direction += pi;
    }

    //      \
    //      _\
    //   d1/  \ r1
    //    / d2_\
    //   /  /   \
    //___|__|___a\
    //    o
    // r1: radius for dist1
    // d1: dist1
    // d2: dist2
    // o: offset, distance between d1 and d2
    // a: angle

    r1 = get_radius(dist1, dist2, m->offset);
    r2 = get_radius(dist2, dist1, m->offset);

    if ( r1 > 0.0 )
        a1 = get_new_angle(dist1, r1);
    else
        a1 = get_new_angle(dist2, r2);

    a2 = m->direction - pi/2.0;
    a = a1+a2;
    m->direction += a1;

    // Get start position
    s1 = get_position(r1, a2);
    s2 = get_position(r2, a2);

#ifdef DEBUG_PRINT
    if ( debug > 0 ) {
        printf("s1: %f, %f\n", s1.x, s1.y);
        printf("p1: %f, %f\n", m->left.x, m->left.y);
        printf("s2: %f, %f\n", s2.x, s2.y);
        printf("p2: %f, %f\n", m->right.x, m->right.y);
    }
#endif // DEBUG_PRINT

    o1 = get_position(r1, a);
    o2 = get_position(r2, a);

    m->left.x += o1.x - s1.x;
    m->left.y += o1.y - s1.y;
    m->right.x += o2.x - s2.x;
    m->right.y += o2.y - s2.y;

    if ( clockwise ) {
        struct position tmp_p;
        tmp_p = m->left;
        m->left = m->right;
        m->right = tmp_p;
        m->direction -= pi;
    }

    while ( m->direction > 2.0 * pi )
        m->direction -= 2.0 * pi;

    while ( m->direction < 0 )
        m->direction += 2.0 * pi;

#ifdef DEBUG_PRINT
    if ( debug > 0 ) {
        printf("c: %d\n", clockwise);
        printf("d1: %f\n", dist1);
        printf("d2: %f\n", dist2);
        printf("r1: %f\n", r1);
        printf("r2: %f\n", r2);
        printf("a1: %f\n", a1);
        printf("a1: %f\n", radians_to_degree(a1));
        printf("a2: %f\n", a2);
        printf("a2: %f\n", radians_to_degree(a2));
        printf("a: %f\n", a);
        printf("a: %f\n", radians_to_degree(a));
        printf("o1: %f %f\n", o1.x, o1.y);
        printf("o2: %f %f\n", o2.x, o2.y);
        printf("s1: %f %f\n", s1.x, s1.y);
        printf("s2: %f %f\n", s2.x, s2.y);
    }
    if ( debug > 0 )
        debug--;
#endif // DEBUG_PRINT
}

static double get_radius_between(double dist1, double dist2, double offset)
{
    double r = (offset*dist1) / (dist2+dist1);

    return r;
}

static double get_radius(double dist1, double dist2, double offset)
{
    double r = (offset*dist1) / (dist2-dist1);

    if ( r < 0 )
        return -r;

    return r;
}

static double get_new_angle(double dist, double r)
{
    double r1;
    double a;

    //      \
    //      _\
    //   d /  \ r
    //    /    \
    //   /      \
    //___|______a\
    //
    // r: radius for dist
    // d: dist
    // a: angle

    a = dist / r;

    return a;
}

static double radians_to_degree(double rad)
{
    return (360.0*rad)/(2.0*pi);
}

static double get_old_angle(struct position p1, struct position p2)
{
    double c;
    struct position p;
    double A;
    int quadrant;

    //  p1,p
    //  |\
    // a| \c
    //  |  \
    //  |__A\
    //       p2,0
    //
    // A: angle
    // p1: point
    // p2: point
    // p: point
    // 0: point 0,0
    // a: length
    // c: length
    //
    //  sin A = a/c

    if ( p1.y < p2.y ) {
        if ( p1.x < p2.x ) {
            p.x = p1.x - p2.x;
            p.y = p1.y - p2.y;
            quadrant = 1;
        } else {
            p.x = p2.x - p1.x;
            p.y = p1.y - p2.y;
            quadrant = 2;
        }
    } else {
        if ( p1.x < p2.x ) {
            p.x = p1.x - p2.x;
            p.y = p2.y - p1.y;
            quadrant = 4;
        } else {
            p.x = p2.x - p1.x;
            p.y = p2.y - p1.y;
            quadrant = 3;
        }
    }

    printf("old quadrant: %d\n", quadrant);
    printf("old p1: %f, %f\n", p1.x, p1.y);
    printf("old p2: %f, %f\n", p2.x, p2.y);
    printf("old p: %f, %f\n", p.x, p.y);

    c = sqrt( p.x*p.x + p.y*p.y );

    A = asin(p.y/c);

    printf("old A: %f\n", A);
    switch ( quadrant ) {
        case 1:
            break;
        case 2:
            A = pi + A;
            break;
        case 3:
            A = pi - A;
            break;
        case 4:
            A = 2.0 * pi + A;
            break;
    };
    printf("old A: %f\n", A);

    if ( A < 0 )
        return -A;
    return A;
}

static struct position get_position(double r, double angle)
{
    struct position p;

    p.x = r * cos(angle);
    p.y = r * sin(angle);

    return p;
}
