#ifndef PURPLESIM_H
#define PURPLESIM_H

#define MAX_MOWERS  3

enum purplesim_sensor {
    purplesim_sensor_range_left,
    purplesim_sensor_range_right,
    purplesim_sensor_bwf_left,
    purplesim_sensor_bwf_right,
    purplesim_sensor_voltage,
    purplesim_sensor_moisture,
};

void initialize_mower(int mower);
void move_mower(int left, int right, int mower);

int purplesim_get_sensor_value(enum purplesim_sensor sensor, int mower);

#endif // PURPLESIM_H
