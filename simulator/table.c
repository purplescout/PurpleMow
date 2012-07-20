
#include "table.h"

int table_lookup(struct table* t, int index)
{
    if ( index >= t->size )
        index = t->size - 1;

    if ( index < 0 )
        index = 0;

    switch ( t->type ) {
        case table_type_int:
            return t->data.i[index];
        case table_type_char:
            return t->data.c[index];
        default:
            return 0;
    }
}

int table_sensor_name_to_value(char* name, struct sensor* sensors)
{
    int found = 0;
    int i = 0;

    while ( strcmp(sensors[i].name, "") ) {
        if ( strcmp(sensors[i].name, name) == 0 )
            return sensors[i].sensor;
        i++;
    }

    return sensors[i].sensor;
}

struct table* table_get_table(int sensor, struct sensor* sensors)
{
    int i = 0;

    while ( sensors[i].sensor != sensor && sensors[i].sensor != 0 ) {
        if ( sensors[i].sensor == sensor )
            return sensors[i].table;
        i++;
    }

    return sensors[i].table;
}

