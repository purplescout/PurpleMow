
#include <sys/socket.h>
#include <netdb.h>

#include <stdio.h>

#include <stdlib.h>  // exit
#include <string.h>

#include "modules.h"

#include "io_transport.h"
#include "commands.h"

#define PORT "35424"
#define SERVER "localhost"

/**
 * @defgroup io_net Network IO
 * Network implementation for the IO Interface.
 *
 * @ingroup io
 */

/**
 * io_net
 *
 * @ingroup io_net
 */
struct io_net
{
    int sock;
    int debug;
};

// functions
static error_code io_net_start();
static error_code io_net_stop();

static struct io_net this = { .sock = -1, .debug = 0 };

/**
 * Initialize the net io.
 *
 * @ingroup io_net
 *
 * @return          Success status
 */
error_code io_transport_init()
{
    module_register_to_phase(phase_START, io_net_start);
    module_register_to_phase(phase_STOP, io_net_stop);
    return err_OK;
}

static error_code io_net_start()
{
    struct addrinfo hints;
    struct addrinfo* addr;
    int result;

    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    result = getaddrinfo(SERVER, PORT, &hints, &addr);
    if ( result != 0 ) {
        perror("Failed to get addr info");
        exit(1);
    }

    this.sock = socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol);
    if ( this.sock < 0 ) {
        perror("Failed to create socket");
        exit(1);
    }

    result = connect(this.sock, addr->ai_addr, addr->ai_addrlen);
    if ( result < 0 ) {
        perror("Failed to connect");
        exit(1);
    }

    freeaddrinfo(addr);

    return err_OK;
}

static error_code io_net_stop()
{
    if ( this.sock != -1 ) {
        close(this.sock);
        this.sock = -1;
    }

    return err_OK;
}

/**
 * Send a message over network to I/O board.
 *
 * @ingroup io_net
 *
 * @param[in] msg       Message to send
 * @param[in] length    Length of message
 *
 * @return              Success status
 */
error_code io_transport_send_command(uint8_t* msg, int length)
{
    int result;

    if ( this.sock == -1 )
        return err_NOT_INITIALIZED;

    if ( length != MAX_MSG_SIZE )
        return err_WRONG_ARGUMENT;

    result = write(this.sock, msg, length);

    if ( result < 0 ) {
        perror("Failed to write command");
        return err_NETWORK;
    }

    return err_OK;
}

/**
 * Read a message from net.
 *
 * @ingroup io_net
 *
 * @param[out] msg      Buffer to write to
 * @param[in]  length   Length of buffer
 *
 * @return              Success status
 */
error_code io_transport_read_data(uint8_t* msg, int length)
{
    int result;

    if ( this.sock == -1 )
        return err_NOT_INITIALIZED;

    if ( length != MAX_MSG_SIZE )
        return err_WRONG_ARGUMENT;

    result = read(this.sock, msg, length);

    if ( result < 0 ) {
        perror("Failed to read command");
        return err_NETWORK;
    }

    return err_OK;
}

