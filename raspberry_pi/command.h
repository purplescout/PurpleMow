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

enum sensor {
    sensor_range_left,
    sensor_range_right,
    sensor_moist,
    sensor_voltage,
    sensor_bwf_left,
    sensor_bwf_right,
    sensor_bwf_reference,
};

#endif // COMMAND_H
