
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <sys/select.h>
#include <sys/types.h>
#include <netdb.h>

#include <pthread.h>

#include "purplesim.h"
#include "mower_net.h"

#include "commands.h"

struct mower {
    int         enabled;
    int         fd;
    int         speed_left;
    int         speed_right;
    int         relay_left;
    int         relay_right;
};

struct mower_net {
    char        port[6];
    int         debug_commands;
    int         debug_values;
    int         right_motor_inverted;
};

static void init_mower(struct mower* mow);
static int enable_new_mower(int fd);
static int disable_mower(int fd);
static int get_mower(int fd);
static void set_non_blocking(int sock);
static void send_data(int mower, unsigned char* buffer, int* length);
static void handle_data(int mower, unsigned char* buffer, int* length);
static void* mower_network(void *data);
static void* mower_work(void *data);


static struct mower mowers[MAX_MOWERS];
static struct mower_net this;

static pthread_t network_thread;
static pthread_t mower_thread;

void mower_net(char* port)
{
    int i = 0;
    while ( i < MAX_MOWERS ) {
        init_mower(&mowers[i]);
        i++;
    }

    this.right_motor_inverted = 0;
    this.debug_commands = 0;
    this.debug_values = 0;

    if ( strlen(port) > 0 )
        snprintf(this.port, sizeof(this.port), "%s", port);
    else
        snprintf(this.port, sizeof(this.port), "%d", DEFAULT_PORT);

    pthread_create( &network_thread, NULL, mower_network, NULL);
    pthread_create( &mower_thread, NULL, mower_work, NULL);
}

void mower_set_motors(int right_motor_inverted)
{
    this.right_motor_inverted = right_motor_inverted;
}

void mower_net_debug(int debug_commands, int debug_values)
{
    this.debug_commands = debug_commands;
    this.debug_values = debug_values;
}

static void* mower_work(void *data)
{
    while ( 1 ) {
        int i = 0;
        int inv = 1;

        if ( this.right_motor_inverted == 1 )
            inv = -1;

        while ( i < MAX_MOWERS ) {
            if ( mowers[i].enabled ) {
                move_mower(mowers[i].speed_left * mowers[i].relay_left,
                           mowers[i].speed_right * mowers[i].relay_right * inv,
                           i);
            }
            i++;
        }

        usleep(250000);
    }
}

static int enable_new_mower(int fd)
{
    int i = 0;
    int done = 0;

    while ( !done && i < MAX_MOWERS ) {
        if ( mowers[i].enabled == 0 ) {
            mowers[i].enabled = 1;
            mowers[i].fd = fd;
            initialize_mower(i);
            done = 1;
        }
        i++;
    }
    return done;
}

static int disable_mower(int fd)
{
    int i = 0;
    int done = 0;

    while ( !done && i < MAX_MOWERS ) {
        if ( mowers[i].enabled == 1 &&
             mowers[i].fd == fd ) {
            init_mower(&mowers[i]);
            done = 1;
        }
    }

    return done;
}

static int get_mower(int fd)
{
    int i = 0;

    while ( i < MAX_MOWERS ) {
        if ( mowers[i].enabled == 1 &&
             mowers[i].fd == fd ) {
            return i;
        }
        i++;
    }
    return -1;
}

static void init_mower(struct mower* mow)
{
    mow->enabled = 0;
    mow->fd = -1;
    mow->speed_left = 0;
    mow->speed_right = 0;
    mow->relay_left = 1;
    mow->relay_right = 1;
}

static void set_non_blocking(int sock)
{
    int opts;

    opts = fcntl(sock,F_GETFL);
    if (opts < 0) {
        perror("Failed to get socket options");
        exit(1);
    }
    opts = (opts | O_NONBLOCK);
    if (fcntl(sock,F_SETFL,opts) < 0) {
        perror("Failed to set non blocking");
        exit(1);
    }

}

static void send_data(int mower, unsigned char* buffer, int* length)
{
    char*   cmd[2];
    int     cmd_value;
    int     result;

    if ( *length < 4 ) {
        *length = 0;
        return;
    }

    cmd[0] = "";
    cmd[1] = "";
    result = -1;
    cmd_value = -1;

    *length = 0;

    switch ( buffer[0] ) {
        case CMD_WRITE:
            cmd[0] = "write";
            switch ( buffer[1] ) {
                case CMD_MOTOR_RIGHT:
                    cmd[1] = "motor right";
                    cmd_value = buffer[2];
                    mowers[mower].speed_right = buffer[2];
                    break;
                case CMD_MOTOR_LEFT:
                    cmd[1] = "motor left";
                    cmd_value = buffer[2];
                    mowers[mower].speed_left = buffer[2];
                    break;
                case CMD_CUTTER:
                    cmd[1] = "cutter";
                    break;
            }
            break;
        case CMD_RELAY:
            cmd[0] = "relay";
            switch ( buffer[1] ) {
                case CMD_RELAY_RIGHT:
                    cmd[1] = "right";
                    cmd_value = buffer[2];
                    mowers[mower].relay_right = buffer[2] ? 1 : -1;
                    break;
                case CMD_RELAY_LEFT:
                    cmd[1] = "left";
                    cmd_value = buffer[2];
                    mowers[mower].relay_left = buffer[2] ? 1 : -1;
                    break;
            }
            break;
        case CMD_READ:
            {
                cmd[0] = "read";
                int value = -1;
                switch ( buffer[1] ) {
                    case CMD_RANGE_SENSOR_RIGHT:
                        cmd[1] = "range right";
                        value = purplesim_get_sensor_value( purplesim_sensor_range_right, mower );
                        break;
                    case CMD_RANGE_SENSOR_LEFT:
                        cmd[1] = "range left";
                        value = purplesim_get_sensor_value( purplesim_sensor_range_left, mower );
                        break;
                    case CMD_MOIST_SENSOR:
                        cmd[1] = "moist";
                        value = purplesim_get_sensor_value( purplesim_sensor_moisture, mower );
                        break;
                    case CMD_VOLTAGE_SENSOR:
                        cmd[1] = "voltage";
                        value = purplesim_get_sensor_value( purplesim_sensor_voltage, mower );
                        break;
                    case CMD_BWF_LEFT_SENSOR:
                        cmd[1] = "BWF left";
                        value = purplesim_get_sensor_value( purplesim_sensor_bwf_left, mower );
                        break;
                    case CMD_BWF_RIGHT_SENSOR:
                        cmd[1] = "BWF right";
                        value = purplesim_get_sensor_value( purplesim_sensor_bwf_right, mower );
                        break;
                }

                if ( value > -1 ) {
                    buffer[0] = CMD_SEND;
                    // buffer[1] the same as requested
                    buffer[2] = value >> 8;
                    buffer[3] = value & 0xff;
                    *length = 4;
                    result = value;
                }
            }
            break;
    }

    if ( this.debug_commands )
        if ( strcmp(cmd[0], "") != 0 )
            if ( cmd_value > -1 )
                printf("%s: %s, %d\n", cmd[0], cmd[1], cmd_value);
            else
                printf("%s: %s\n", cmd[0], cmd[1]);

    if ( this.debug_values )
        if ( result > -1 )
            printf("Result value: %d\n", result);

}

static void handle_data(int mower, unsigned char* buffer, int* length)
{
#ifndef READABLE_COMMANDS
    send_data(mower, buffer, length);
#else

    unsigned char b[4] = { 0 };
    int l = sizeof(b);
    int v = 0;

    unsigned char* command;
    unsigned char* value;
    unsigned char* end;

    command = buffer;
    value = strchr(command, ' ');

    if ( value != NULL ) {
        value++;
        end = strchr(value, '\n');
        if ( end != NULL )
            *end = '\0';
        v = atoi(value);
        if ( v > 255 )
            v = 255;
    }


    // Range Left
    if ( strncmp( command, "RL", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_RANGE_SENSOR_LEFT;
    } else

    // Range Right
    if ( strncmp( command, "RR", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_RANGE_SENSOR_RIGHT;
    } else

    // BWF Left
    if ( strncmp( command, "BL", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_BWF_LEFT_SENSOR;
    } else

    // BWF Right
    if ( strncmp( command, "BR", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_BWF_RIGHT_SENSOR;
    } else

    // Moist Sensor
    if ( strncmp( command, "MS", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_MOIST_SENSOR;
    } else

    // Voltage Sensor
    if ( strncmp( command, "VS", 2 ) == 0 ) {
        b[0] = CMD_READ;
        b[1] = CMD_VOLTAGE_SENSOR;
    } else

    // Motor Right
    if ( strncmp( command, "MR", 2 ) == 0 ) {
        b[0] = CMD_WRITE;
        b[1] = CMD_MOTOR_RIGHT;
        b[2] = v;
    } else

    // Motor Left
    if ( strncmp( command, "ML", 2 ) == 0 ) {
        b[0] = CMD_WRITE;
        b[1] = CMD_MOTOR_LEFT;
        b[2] = v;
    } else

    // Motor Cutter
    if ( strncmp( command, "MC", 2 ) == 0 ) {
        b[0] = CMD_WRITE;
        b[1] = CMD_CUTTER;
        b[2] = v;
    } else

    // rElay Right
    if ( strncmp( command, "ER", 2 ) == 0 ) {
        b[0] = CMD_RELAY;
        b[1] = CMD_RELAY_RIGHT;
        b[2] = v;
    } else

    // rElay Left
    if ( strncmp( command, "EL", 2 ) == 0 ) {
        b[0] = CMD_RELAY;
        b[1] = CMD_RELAY_LEFT;
        b[2] = v;
    } else

    // No valid command
    {
        return;
    }

    send_data(mower, b, &l);

    *length = 0;

#endif // READABLE_COMMANDS
}

static void* mower_network(void *data)
{
    int max_fd = 0;
    fd_set clients;
    struct addrinfo hints;
    struct addrinfo* addr;
    int sock;
    int socks;
    int i;
    int j;
    int result;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
//    hints.ai_family = AF_INET6;
//    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    result = getaddrinfo(NULL, this.port, &hints, &addr);
    if ( result != 0 ) {
        perror("Failed to get addr info");
        exit(1);
    }

    sock = socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol);
    if ( sock < 0 ) {
        perror("Failed to create socket");
        exit(1);
    }

    set_non_blocking(sock);

    result = bind(sock, addr->ai_addr, addr->ai_addrlen);
    if ( result < 0 ) {
        perror("Failed to bind port");
        close(sock);
        exit(1);
    }

    freeaddrinfo(addr);

    result = listen(sock, 0);
    if ( result == -1 ) {
        perror("Failed to listen on socket");
        exit(1);
    }

    max_fd = sock + 1;
    FD_ZERO(&clients);

    while ( 1 ) {
        fd_set  readsocks;

        FD_ZERO(&readsocks);

        FD_SET(sock, &readsocks);

        i = 0;
        while ( i < max_fd ) {
            if ( FD_ISSET(i, &clients) ) {
                FD_SET(i, &readsocks);
            }
            i++;
        }

        socks = select(max_fd, &readsocks, NULL, NULL, NULL);

        if (socks < 0) {
            perror("Select failed");
            exit(1);
        }

        if (socks > 0) {
            if (FD_ISSET(sock, &readsocks)) {
                // New connection
                int fd;
                fd = accept(sock, NULL, NULL);

                if (fd < 0) {
                    perror("Failed to accept");
                } else {
                    // Connected
                    if ( enable_new_mower(fd) ) {
                        set_non_blocking(fd);
                        FD_SET(fd, &clients);

                        if (fd >= max_fd)
                            max_fd = fd + 1;
                    } else {
                        // Too many connected
                        close(fd);
                    }
                }
            }

            i = 0;
            while ( i < max_fd ) {
                if ( FD_ISSET(i, &clients) ) {
                    unsigned char buffer[32] = { 0 };

                    int size;

                    size = read(i, buffer, sizeof(buffer));

                    if ( size == 0 ) {
                        // Connection closed
                        disable_mower(i);
                        close(i);

                        FD_CLR(i, &clients);
                        if ( i + 1 >= max_fd ) {
                            j = 0;
                            max_fd = sock + 1;
                            while ( j < max_fd ) {
                                if ( FD_ISSET(j, &clients ) )
                                    if ( j >= max_fd )
                                        max_fd = j + 1;
                                j++;
                            }
                        }
                    }
                    j = -1;
                    while ( size > ++j*4 ) {
                        int size2 = 4;
                        handle_data(get_mower(i), &buffer[j*4], &size2);
                        if ( size2 == 4 ) {
                            //printf("writing: %x %x %x %x\n", buffer[j*4], buffer[j*4+1], buffer[j*4+2], buffer[j*4+3]);
                            write(i, &buffer[j*4], 4);
                        }
                    }
                }
                i++;
            }
        }
    }

    return NULL;
}
