#ifndef UTILS_H
#define UTILS_H

#include "error_codes.h"

#define min(x,y) (x) < (y) ? (x) : (y)
#define max(x,y) (x) > (y) ? (x) : (y)

error_code get_random(int *random);

#endif // UTILS_H
