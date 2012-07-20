
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>

#ifdef SIMULATOR
#include <errno.h>
#endif // SIMULATOR

#include "io.h"

#define LEFT_MOTOR  "30"
#define RIGHT_MOTOR "31"
#define BLADE       "32"

#ifdef SIMULATOR
#define GPIO_DIR "gpio"
#else
#define GPIO_DIR "/sys/class/gpio"
#endif // SIMULATOR

// private functions
static int setup_port(FILE *export, char *port);

/**
 * @defgroup raspi_gpio Raspberry PI GPIO
 * GPIO ports on Raspberry PI
 *
 * @ingroup io
 */

/**
 * Initialize the GPIO ports.
 *
 * @ingroup raspi_gpio
 *
 * @return          Success status
 */
int purple_io_init()
{
    FILE *fp;

#ifdef SIMULATOR
    {
        struct stat statbuf;
        if ( stat(GPIO_DIR, &statbuf) == -1 )
        {
            if ( errno == ENOENT )
            {
                if ( mkdir(GPIO_DIR, S_IRWXU | S_IRWXG | S_IRWXO) == -1 )
                {
                    perror("creating directory " GPIO_DIR);
                }
            }
            else
            {
                perror(GPIO_DIR);
            }
        }
    }
#endif // SIMULATOR


#ifdef SIMULATOR
    fp = fopen(GPIO_DIR "/export", "wb");
#else
    fp = fopen(GPIO_DIR "/export", "ab");
#endif // SIMULATOR

    if ( fp == NULL )
    {
        perror("opening gpio export file");
    }
    else
    {
        setup_port(fp, LEFT_MOTOR);
        setup_port(fp, RIGHT_MOTOR);
        setup_port(fp, BLADE);
        fclose(fp);
    }
}

/**
 * Setup the GPIO ports and return a FILE* to the GPIOs.
 *
 * @ingroup raspi_gpio
 *
 * @param[out] export   Returned FILE*
 * @param[in]  port     GPIO port
 *
 * @return              Success status
 */
static int setup_port(FILE *export, char *port)
{
    FILE *fp;
    char direction_file[64] = { 0 };
    char gpio_dir[64] = { 0 };
    char command[16] = { 0 };
    int length;
    struct stat statbuf;

    // create the entries in sysfs
#ifdef SIMULATOR
#else
    rewind(export);
#endif // SIMULATOR
    fwrite(port, sizeof(port[0]), strlen(port), export);

    // set direction of ports

    snprintf(gpio_dir, sizeof(gpio_dir) - 1, "%s/gpio%s", GPIO_DIR, port);
    snprintf(direction_file, sizeof(direction_file) - 1, "%s/direction", gpio_dir);

    if ( stat(gpio_dir, &statbuf) == -1 )
    {
#ifdef SIMULATOR
        if ( errno == ENOENT )
        {
            if ( mkdir(gpio_dir, S_IRWXU | S_IRWXG | S_IRWXO) == -1 )
            {
                perror("creating directory " GPIO_DIR);
            }
        }
        else
#endif // SIMULATOR
        {
            perror(GPIO_DIR);
            return 1;
        }
    }
#ifdef SIMULATOR
    if ( (fp = fopen(direction_file, "wb+")) == NULL )
#else
    if ( (fp = fopen(direction_file, "rb+")) == NULL)
#endif // SIMULATOR
    {
        perror(direction_file);
        return 1;
    }

    rewind(fp);
    snprintf(command, sizeof(command) - 1, "out");
    length = strlen(command);
    if ( fwrite(command, sizeof(command[0]), length, fp) < length )
    {
        fprintf(stderr, "Error writing to %s\n", direction_file);
    }
    fclose(fp);

    return 0;
}




