#ifndef DCN_H
#define DCN_H

struct IP {
    unsigned char ip[4];
};

int dcn_init();
int dcn_get_ip(struct IP *ip);
int dcn_get_uuid(char *buffer, int length);
int dcn_get_version(int *major, int *minor);
int dcn_get_sysname(char *buffer, int length);

#endif // DCN_H
