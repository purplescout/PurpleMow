#ifndef MOWER_CALC
#define MOWER_CALC

#include "position.h"
#include "mower_pos.h"

#define pi 3.1415926535897932384626433832795028841971693993751058209749

void update_positions(struct mower_pos* m, double dist1, double dist2);
void init_mower_pos(struct mower_pos* p, double offset);

#endif // MOWER_CALC
