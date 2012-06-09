
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#include "utils.h"

/**
 * @defgroup utils Utils
 * Utility functions.
 *
 * @ingroup purplemow
 */


/**
 * Get random data.
 *
 * @ingroup utils
 *
 * @param[out] random       Random data
 *
 * @return                  Success status
 */
error_code get_random(int *random)
{
    int fd = open("/dev/urandom", O_RDONLY);

    if ( fd < 0 )
        return err_FILE;


    read(fd, random, sizeof(*random));

    close(fd);

    return err_OK;
}
