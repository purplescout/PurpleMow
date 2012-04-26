#ifndef DCN_H
#define DCN_H

#include "error_codes.h"

struct IP {
    unsigned char ip[4];
};

error_code dcn_init();
error_code dcn_get_ip(struct IP *ip);
error_code dcn_get_uuid(char *buffer, int length);
error_code dcn_get_version(int *major, int *minor);
error_code dcn_get_sysname(char *buffer, int length);

#endif // DCN_H
