#ifndef DCN_H
#define DCN_H

#include <stdint.h>

#include "error_codes.h"

enum ip_family {
    ip_family_4,
    ip_family_6,
};

struct IP {
    enum ip_family  family;
    union {
        uint8_t     v4[4];
        uint8_t     v6[16];
    } address;
    union {
        uint8_t     v4[4];
        uint8_t     v6[16];
    } netmask;
};

error_code dcn_init();
error_code dcn_start();

error_code dcn_get_ip(struct IP *ip);
error_code dcn_get_uuid(char *buffer, int length);
error_code dcn_get_version(int *major, int *minor);
error_code dcn_get_sysname(char *buffer, int length);

#endif // DCN_H
