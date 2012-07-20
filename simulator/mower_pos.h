#ifndef MOWER_POS
#define MOWER_POS

#include "position.h"

struct mower_pos {
    struct position     left;
    struct position     right;
    double              offset;
    double              direction;
};

#endif // MOWER_POS
