
#include <string.h>
#include <stdio.h>

#include "dcn.h"

#define MAJOR_VERSION 0
#define MINOR_VERSION 1

char sysname[32] = { 0 };
char uuid[40] = { 0 };
struct IP ip;
static int initialized = 0;

int dcn_init()
{
    if ( initialized )
    {
        fprintf(stderr, "DCN: Already initialized\n");
        return -1;
    }
    // TODO: generate an uuid
    snprintf(uuid, sizeof(uuid) - 1, "12345678-1234-1234-1234-123456789012");

    // TODO: get ip
    ip.ip[0] = 192;
    ip.ip[1] = 168;
    ip.ip[2] = 10;
    ip.ip[3] = 11;

    snprintf(sysname, sizeof(sysname) - 1, "PurpleScout");

    initialized = 1;
}

int dcn_get_ip(struct IP *buffer)
{
    if ( !initialized )
    {
        return -1;
    }

    if ( buffer == NULL )
    {
        return -1;
    }

    buffer->ip[0] = ip.ip[0];
    buffer->ip[1] = ip.ip[1];
    buffer->ip[2] = ip.ip[2];
    buffer->ip[3] = ip.ip[3];

    return 0;
}

int dcn_get_uuid(char *buffer, int length)
{
    if ( !initialized )
    {
        return -1;
    }

    if ( buffer == NULL )
    {
        return -1;
    }

    if ( length < strlen(uuid) + 1 )
    {
        return -1;
    }

    strncpy(buffer, uuid, length);

    return 0;
}

int dcn_get_version(int *major, int *minor)
{
    if ( major == NULL )
    {
        return -1;
    }

    if ( minor == NULL )
    {
        return -1;
    }

    *major = MAJOR_VERSION;
    *minor = MINOR_VERSION;

    return 0;
}

int dcn_get_sysname(char *buffer, int length)
{
    if ( !initialized )
    {
        return -1;
    }

    if ( buffer == NULL )
    {
        return -1;
    }

    if ( length < strlen(sysname) + 1 )
    {
        return -1;
    }

    strncpy(buffer, sysname, length);

    return 0;
}
