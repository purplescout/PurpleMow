#include <ifaddrs.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netinet/in.h>

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "modules.h"
#include "dcn.h"
#include "cli.h"

#define MAJOR_VERSION 0
#define MINOR_VERSION 1

/**
 * @defgroup dcn
 * DCN
 *
 * @ingroup purplemow
 */

/**
 * A linked list item.
 *
 * @ingroup dcn
 */
struct ip_item {
    char*           if_name;
    struct IP       ip;
    struct ip_item* next;
};

/**
 * Dcn item
 *
 * @ingroup dcn
 */
struct dcn {
    char                sysname[32];
    char                uuid[40];
    int                 initialized;
    struct ip_item*     ip_list;
    struct ip_item*     ip_main;
};

// Private functions
static error_code dcn_start(void* data);

static error_code free_ip_list(struct ip_item *ip_item);
static error_code find_ips();
static error_code find_main_ip();
static int get_netmask(struct IP *ip);
static int get_bits(uint8_t byte);

static struct dcn this = { .initialized = 0 };

// Commands
static int command_show_ip(char *args, int (*print)(const char *format, ...));
static int command_show_ips(char *args, int (*print)(const char *format, ...));

/**
 * Initialize dcn
 *
 * @ingroup dcn
 *
 * @return      Success status
 */
error_code dcn_init()
{
    if ( this.initialized )
    {
        fprintf(stderr, "DCN: Already initialized\n");
        return err_ALREADY_INITIALIZED;
    }
    // TODO: generate an uuid
    snprintf(this.uuid, sizeof(this.uuid) - 1, "12345678-1234-1234-1234-123456789012");

    find_ips();
    find_main_ip();

    snprintf(this.sysname, sizeof(this.sysname) - 1, "PurpleScout");

    cli_register_command("ip", command_show_ip);
    cli_register_command("ips", command_show_ips);

    module_register_to_phase(phase_START, dcn_start, NULL);

    this.initialized = 1;

    return err_OK;
}

/**
 * Start dcn
 *
 * @ingroup dcn
 *
 * @return      Success status
 */
static error_code dcn_start(void* data)
{
    return err_OK;
}

/**
 * Get the main IP
 *
 * @ingroup dcn
 *
 * @param[out] buffer   Returned value
 *
 * @return              Success status
 */
error_code dcn_get_ip(struct IP *buffer)
{
    if ( !this.initialized )
        return err_NOT_INITIALIZED;

    if ( buffer == NULL )
        return err_WRONG_ARGUMENT;

    if ( this.ip_main == NULL )
        return err_NOT_INITIALIZED;

    *buffer = this.ip_main->ip;

    return err_OK;
}

/**
 * Get the uuid.
 *
 * @ingroup dcn
 *
 * @param[out] buffer   The returned value
 * @param[in] length    Length of buffer
 *
 * @return              Success status
 */
error_code dcn_get_uuid(char *buffer, int length)
{
    if ( !this.initialized )
    {
        return err_NOT_INITIALIZED;
    }

    if ( buffer == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    if ( length < strlen(this.uuid) + 1 )
    {
        return err_BUFFER_TOO_SMALL;
    }

    strncpy(buffer, this.uuid, length);

    return err_OK;
}

/**
 * Get the version number.
 *
 * @ingroup dcn
 *
 * @param[out] major    Major version number
 * @param[out] minor    Minor version number
 *
 * @return              Success status
 */
error_code dcn_get_version(int *major, int *minor)
{
    if ( major == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    if ( minor == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    *major = MAJOR_VERSION;
    *minor = MINOR_VERSION;

    return err_OK;
}

/**
 * Get the system name.
 *
 * @ingroup dcn
 *
 * @param[out] buffer   The returned sysname
 * @param[in] length    Length of buffer
 *
 * @return              Success status
 */
error_code dcn_get_sysname(char *buffer, int length)
{
    if ( !this.initialized )
    {
        return err_NOT_INITIALIZED;
    }

    if ( buffer == NULL )
    {
        return err_WRONG_ARGUMENT;
    }

    if ( length < strlen(this.sysname) + 1 )
    {
        return err_BUFFER_TOO_SMALL;
    }

    strncpy(buffer, this.sysname, length);

    return err_OK;
}

/**
 * Find all configured IP addresses and update this.
 *
 * @ingroup dcn
 *
 * @return      Success status
 */
static error_code find_ips()
{
    struct ifaddrs *if_addr;
    struct ifaddrs *ifa;
    void *tmp;

    struct ip_item* ip_item = NULL;
    struct ip_item* ip_head = NULL;
    struct ip_item* ip_tail = NULL;

    getifaddrs(&if_addr);

    ifa = if_addr;

    while (ifa != NULL) {

        switch ( ifa->ifa_addr->sa_family ) {
            case AF_INET:
            case AF_INET6:
                ip_item = malloc(sizeof(*ip_item));
                ip_item->next = NULL;
                ip_item->if_name = malloc(strlen(ifa->ifa_name)+1);
                strcpy(ip_item->if_name, ifa->ifa_name);
                break;
        }

        switch ( ifa->ifa_addr->sa_family ) {
            case AF_INET:
                ip_item->ip.family = ip_family_4;

                memcpy(&ip_item->ip.address.v4,
                       &((struct sockaddr_in*)ifa->ifa_addr)->sin_addr,
                       sizeof(ip_item->ip.address.v4));

                memcpy(&ip_item->ip.netmask.v4,
                       &((struct sockaddr_in*)ifa->ifa_netmask)->sin_addr,
                       sizeof(ip_item->ip.netmask.v4));
                break;
            case AF_INET6:
                ip_item->ip.family = ip_family_6;

                memcpy(&ip_item->ip.address.v6,
                       &((struct sockaddr_in6*)ifa->ifa_addr)->sin6_addr,
                       sizeof(ip_item->ip.address.v6));

                memcpy(&ip_item->ip.netmask.v6,
                       &((struct sockaddr_in*)ifa->ifa_netmask)->sin_addr,
                       sizeof(ip_item->ip.netmask.v6));
                break;
        }

        if ( ip_item != NULL ) {
            if ( ip_head == NULL ) {
                ip_head = ip_item;
                ip_tail = ip_item;
            } else {
                ip_tail->next = ip_item;
                ip_tail = ip_item;
            }
            ip_item = NULL;
        }

        ifa = ifa->ifa_next;
    }

    if ( if_addr != NULL )
        freeifaddrs(if_addr);

    if ( ip_head != NULL ) {
        free_ip_list(this.ip_list);
        this.ip_list = ip_head;
    }

    return err_OK;
}

/**
 * Empty the list of IP addresses.
 *
 * @ingroup dcn
 *
 * @param[in] ip_item   List to empty
 *
 * @return              Success status
 */
static error_code free_ip_list(struct ip_item *ip_item)
{
    struct ip_item *current = NULL;

    if ( ip_item == NULL )
        return err_OK;

    while ( ip_item != NULL ) {
        current = ip_item;
        ip_item = ip_item->next;

        free(current->if_name);
        free(current);
    }

    return err_OK;
}

/**
 * Find the main IP, default to first IP that is not bound to the lo interface.
 *
 * @ingroup dcn
 *
 * @return      Success status
 */
static error_code find_main_ip()
{
    struct ip_item *current;
    struct ip_item *main_v4 = NULL;
    struct ip_item *main_v6 = NULL;

    current = this.ip_list;

    while ( current != NULL ) {
        if ( strcmp(current->if_name, "lo") ) {
            switch ( current->ip.family ) {
                case ip_family_4:
                    if ( main_v4 == NULL )
                        main_v4 = current;
                    break;
                case ip_family_6:
                    if ( main_v6 == NULL )
                        main_v6 = current;
                    break;
            }
        }
        current = current->next;
    }

    // Default to IPv4 over IPv6
    if ( main_v6 != NULL )
        this.ip_main = main_v6;

    if ( main_v4 != NULL )
        this.ip_main = main_v4;

    return err_OK;
}

/**
 * The command <b>ip</b>, print the main IP address.
 *
 * @ingroup dcn
 *
 * @param[in] args  Agruments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_show_ips(char *args, int (*print)(const char *format, ...))
{
    struct ip_item  *current;
    char            buffer_addr[128];

    current = this.ip_list;

    while ( current != NULL ) {
        buffer_addr[0] = '\0';
        switch (current->ip.family) {
            case ip_family_4:
                inet_ntop(AF_INET, current->ip.address.v4, buffer_addr, sizeof(buffer_addr));
                break;
            case ip_family_6:
                inet_ntop(AF_INET6, current->ip.address.v6, buffer_addr, sizeof(buffer_addr));
                break;
        }

        if ( strlen(buffer_addr) )
            print("%s: %s/%d\n", current->if_name, buffer_addr, get_netmask(&current->ip));

        current = current->next;
    }

    return 0;
}

/**
 * The command <b>ips</b>, print all found IP addresses.
 *
 * @ingroup dcn
 *
 * @param[in] args  Arguments
 * @param[in] print Print function
 *
 * @return          Success status
 */
static int command_show_ip(char *args, int (*print)(const char *format, ...))
{
    struct IP   ip;
    error_code  result;
    char        buffer_addr[128] = {0};

    result = dcn_get_ip(&ip);

    if ( SUCCESS(result) ) {
        switch (ip.family) {
            case ip_family_4:
                inet_ntop(AF_INET, ip.address.v4, buffer_addr, sizeof(buffer_addr));
                break;
            case ip_family_6:
                inet_ntop(AF_INET6, ip.address.v6, buffer_addr, sizeof(buffer_addr));
                break;
        }
    }

    if ( strlen(buffer_addr) )
        print("%s/%d\n", buffer_addr, get_netmask(&ip));

    return 0;
}

/**
 * Get the number of bits set in the netmask in an IP address.
 *
 * @ingroup dcn
 *
 * @param[in] ip    IP address with netmask
 *
 * @return          Number of bits set
 */
static int get_netmask(struct IP *ip)
{
    int bits = 0;
    int i = 0;

    switch ( ip->family ) {
        case ip_family_4:
            while ( i < sizeof(ip->netmask.v4)/sizeof(ip->netmask.v4[0]) ) {
                bits += get_bits(ip->netmask.v4[i]);
                i++;
            }
            break;
        case ip_family_6:
            while ( i < sizeof(ip->netmask.v6)/sizeof(ip->netmask.v6[0]) ) {
                bits += get_bits(ip->netmask.v6[i]);
                i++;
            }
            break;
    }

    return bits;
}

/**
 * Get number of bits set in byte. Only considers consecutive set bits from left.
 *
 * @ingroup dcn
 *
 * @param[in] byte  Byte to check
 *
 * @return          Number of bits set
 */
static int get_bits(uint8_t byte)
{
    switch ( byte ) {
        case 0xff:
            return 8;
        case 0xfe:
            return 7;
        case 0xfc:
            return 6;
        case 0xf8:
            return 5;
        case 0xf0:
            return 4;
        case 0xe0:
            return 3;
        case 0xc0:
            return 2;
        case 0x80:
            return 1;
        case 0x00:
            return 0;
    }

    return 0;
}
