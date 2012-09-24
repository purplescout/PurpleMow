#ifndef CONFIG_H
#define CONFIG_H

#include <stdint.h>

#define CONFIG_NAME_SIZE    64

enum config_type {
    config_type_uint8,
    config_type_uint16,
    config_type_uint32,
    config_type_int,
    config_type_char,
    config_type_string,
};

struct config_item {
    enum config_type    type;
    char                name[CONFIG_NAME_SIZE];
    union {
        uint8_t         u8;
        uint16_t        u16;
        uint32_t        u32;
        int             i;
        char            c;
        char*           s;
    } value;
    int                 presistent;
};

error_code config_init();

error_code config_get_item(char* name, struct config_item** item);
error_code config_create_item(char* name, enum config_type type, struct config_item** item);

#endif // CONFIG_H
