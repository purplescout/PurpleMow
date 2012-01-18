
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>
#include <pthread.h>

#include "auto_management.h"
#include "dcn.h"

#define M_PORT 32001
#define M_PORT_REPLIES 32002
#define M_ADDRESS "225.0.0.45"
#define BUFFER_SIZE 1024

static void* multicast_listen(void *threadid);
static int parse_command(char *command);

static int fd = -1;
static pthread_t thread;

int multicast_init()
{
    struct ip_mreq mreq;
    int res;
    struct sockaddr_in addr;

    fd = socket(AF_INET, SOCK_DGRAM, 0);

    if ( fd == -1 )
    {
        perror("creating socket");
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(M_PORT);
    addr.sin_addr.s_addr = inet_addr(M_ADDRESS);

    res = bind(fd,(struct sockaddr *)&addr, sizeof(addr));

    if ( res == -1 )
    {
        perror("binding multicast socket");
        return -1;
    }

    mreq.imr_multiaddr.s_addr = inet_addr(M_ADDRESS);
    mreq.imr_interface.s_addr = INADDR_ANY;

    res = setsockopt(fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    if ( res == -1 )
    {
        perror("setsockopt on multicast socket");
        return -1;
    }
}

int multicast_start()
{
    int res;

    if ( fd == -1 )
    {
        fprintf(stderr, "Not initialized\n");
        return -1;
    }

    res = pthread_create(&thread, NULL, multicast_listen, NULL);

    if ( res != 0 )
    {
        fprintf(stderr, "Failed to create thread\n");
        return -1;
    }

    return 0;
}

static void* multicast_listen(void *threadid)
{
    int read;
    char buffer[BUFFER_SIZE] = { 0 };
    socklen_t addrlen;
    struct sockaddr_in addr;

    while ( 1 )
    {
        addrlen = sizeof(addr);

        read = recvfrom(fd, buffer, sizeof(buffer) - 1, 0, (struct sockaddr *)&addr, &addrlen);

        if ( read < sizeof(buffer) )
        {
            buffer[read] = '\0';
        }

        if ( read != -1 )
        {
            parse_command(buffer);
        }
    }
}

static int parse_command(char *command)
{
    char buffer[128] = { 0 };

    if ( strcmp(command, "PurpleMow ping\n") == 0 )
    {
        struct IP ip = { 0 };
        char sysname[32] = { 0 };
        char uuid[40] = { 0 };
        int major = 0;
        int minor = 0;

        dcn_get_version(&major, &minor);
        dcn_get_uuid(uuid, sizeof(uuid));
        dcn_get_sysname(sysname, sizeof(sysname));
        dcn_get_ip(&ip);

        snprintf(buffer, sizeof(buffer), "Sysname: %s\n"
                                         "UUID: %s\n"
                                         "Version: %d.%d\n"
                                         "IP: %d.%d.%d.%d\n",
                                         sysname,
                                         uuid,
                                         major,
                                         minor,
                                         ip.ip[0],
                                         ip.ip[1],
                                         ip.ip[2],
                                         ip.ip[3]);
    }

    if ( strlen(buffer) > 0 )
    {
        int fd = -1;
        socklen_t addrlen;
        struct sockaddr_in addr;
        ssize_t written;
        fd = socket(AF_INET, SOCK_DGRAM, 0);

        if ( fd == -1 )
        {
            perror("creating socket");
            return -1;
        }

        memset(&addr, 0, sizeof(addr));
        addr.sin_family = AF_INET;
        addr.sin_port = htons(M_PORT_REPLIES);
        addr.sin_addr.s_addr = inet_addr(M_ADDRESS);

        addrlen = sizeof(addr);

        written = sendto(fd, buffer, strlen(buffer), 0, (struct sockaddr*)&addr, addrlen);
    }
}
