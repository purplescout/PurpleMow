
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdio.h>

#define M_PORT 32002
#define M_ADDRESS "225.0.0.45"
#define BUFFER_SIZE 1024

/**
 * @defgroup mlisten
 * Test program that listens to a multicast address and prints received data as strings.
 */

/**
 * Main
 *
 * @ingroup mlisten
 *
 * @param[in] argc      Argument count
 * @param[in] argv      Argument vector
 *
 * @return              Success status
 */
int main(int argc, char **argv)
{
    int fd;
    struct ip_mreq mreq;
    int res;
    struct sockaddr_in addr;
    int read;
    char buffer[BUFFER_SIZE] = { 0 };
    socklen_t addrlen;

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
            printf("%s", buffer);
        }
    }
}
