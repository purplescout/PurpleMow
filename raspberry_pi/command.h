#ifndef COMMAND_H
#define COMMAND_H

enum direction {
    direction_forward,
    direction_backward,
    direction_right,
    direction_left,
    direction_undefined,
};

enum command {
    command_start,
    command_stop,
};

#endif // COMMAND_H
