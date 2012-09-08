#ifndef IO_TRANSPORT_H
#define IO_TRANSPORT_H

#include "error_codes.h"

/**
 * @defgroup io_transport IO Transport
 * IO Transport.
 *
 * @ingroup io
 */

error_code io_transport_init();
error_code io_transport_send_command(uint8_t* msg, int length);
error_code io_transport_read_data(uint8_t* msg, int length);

#endif // IO_TRANSPORT_H
