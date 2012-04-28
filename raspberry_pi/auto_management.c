
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>

#include "thread.h"
#include "auto_management.h"
#include "dcn.h"

#define M_PORT 32001
#define M_PORT_REPLIES 32002
#define M_ADDRESS "225.0.0.45"
#define BUFFER_SIZE 1024

struct multicast {
    int         fd;
    pthread_t   thread;
};

static void* multicast_listen(void *data);
static error_code parse_command(char *command);

static struct multicast this;

error_code multicast_init()
{
    struct ip_mreq mreq;
    int res;
    struct sockaddr_in addr;

    this.fd = socket(AF_INET, SOCK_DGRAM, 0);

    if ( this.fd == -1 )
    {
        perror("creating socket");
        return err_SOCKET;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(M_PORT);
    addr.sin_addr.s_addr = inet_addr(M_ADDRESS);

    res = bind(this.fd,(struct sockaddr *)&addr, sizeof(addr));

    if ( res == -1 )
    {
        perror("binding multicast socket");
        return err_SOCKET;
    }

    mreq.imr_multiaddr.s_addr = inet_addr(M_ADDRESS);
    mreq.imr_interface.s_addr = INADDR_ANY;

    res = setsockopt(this.fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    if ( res == -1 )
    {
        perror("setsockopt on multicast socket");
        return err_CONFIGURE_DEVICE;
    }
    return err_OK;
}

error_code multicast_start()
{
    error_code result;

    if ( this.fd == -1 )
    {
        fprintf(stderr, "Not initialized\n");
        return err_SOCKET;
    }


    result = thread_start(&this.thread, multicast_listen);

    if ( FAILURE(result) ) {
        return result;
    }

    return err_OK;
}

static void* multicast_listen(void *data)
{
    int read;
    char buffer[BUFFER_SIZE] = { 0 };
    socklen_t addrlen;
    struct sockaddr_in addr;

    while ( 1 )
    {
        addrlen = sizeof(addr);

        read = recvfrom(this.fd, buffer, sizeof(buffer) - 1, 0, (struct sockaddr *)&addr, &addrlen);

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

static error_code parse_command(char *command)
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
            return err_SOCKET;
        }

        memset(&addr, 0, sizeof(addr));
        addr.sin_family = AF_INET;
        addr.sin_port = htons(M_PORT_REPLIES);
        addr.sin_addr.s_addr = inet_addr(M_ADDRESS);

        addrlen = sizeof(addr);

        written = sendto(fd, buffer, strlen(buffer), 0, (struct sockaddr*)&addr, addrlen);
    }

    return err_OK;
}
