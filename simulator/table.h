#ifndef TABLE_H
#define TABLE_H

#define table_5000mV_in_8bits           0.0512
#define table_5000mV_in_10bits          0.2048
#define table_12000mV_in_10bits         0.085334
#define table_100_percent_in_10bits     10.2400

enum table_type {
    table_type_int,
    table_type_char,
};

struct table {
    int                 size;
    enum table_type     type;
    double              factor;
    union {
        int*            i;
        char*           c;
    } data;
};

struct sensor {
    char                name[32];
    int                 sensor;
    struct table*       table;
};

int table_lookup(struct table* t, int index);
int table_sensor_name_to_value(char* name, struct sensor* sensors);
struct table* table_get_table(int sensor, struct sensor* sensors);

#endif // TABLE_H
