
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "error_codes.h"
#include "modules.h"
#include "utils/list.h"
#include "config.h"

#define CONFIG_FILE  ".config"

/**
 * @defgroup config Config
 *
 * Config
 *
 * @ingroup purplemow
 */

// Private functions
static error_code config_register_commands(void* data);
static error_code config_load(void* data);
static error_code config_save(void* data);
static error_code config_set_value(char* args);

// cli commands
static int command_config(char *args);

/**
 * Config.
 *
 * @ingroup config
 */
struct config {
    list_t              list;
    pthread_mutex_t     list_mutex;
};

// TODO
//
// skapa en lista med namn till varden
// en struct till vardena, med typen

static struct config this;

/**
 * Initialize config.
 *
 * @ingroup config
 * @return                  Success status.
 */
error_code config_init()
{
    pthread_mutex_init(&this.list_mutex, NULL);

    list_create(&this.list);

    module_register_to_phase(phase_REGISTER_COMMANDS, config_register_commands, NULL);
    module_register_to_phase(phase_LOAD_CONFIG, config_load, NULL);
    module_register_to_phase(phase_SAVE_CONFIG, config_save, NULL);

    return err_OK;
}

/**
 * Register commands for config.
 *
 * @ingroup config
 * @return                  Success status.
 */
static error_code config_register_commands(void* data)
{
    error_code result;

    cli_register_command("config", command_config);

    return err_OK;
}

static error_code compare_name(void* data1, void* data2)
{
    char*               name;
    struct config_item* item;

    int status;

    name = (char*)data1;
    item = (struct config_item*)data2;

    status = strcmp(name, item->name);

    if( status < 0 )
        return err_LESS_THAN;
    if( status > 0 )
        return err_GREATER_THAN;
    return err_EQUAL;
}

/**
 * Load the configuration.
 *
 * @ingroup config
 * @return                  Success status.
 */
static error_code config_load(void* data)
{
    char*   b;
    char*   c;
    char*   t;
    int     v;
    int     length;

    struct config_item* item;
    enum config_type    type;

    error_code status;

    char    buffer[256];
    FILE*   file;

    file = fopen(CONFIG_FILE, "r");

    if ( file == NULL ) {
//        perror("Failed to open file");
        return err_FILE;
    }

    while ( b = fgets(buffer, sizeof(buffer), file) ) {
        c = strchr(buffer, ':');
        if ( c == NULL )
            continue;

        *c = '\0';
        c += 2;

        t = c;
        c = strchr(c, ':');
        if ( c == NULL )
            continue;

        *c = '\0';
        c += 2;

        v = atoi(c);

        if ( strcmp("int", t) == 0 )
            type = config_type_int;
        else if ( strcmp("uint8", t) == 0 )
            type = config_type_uint8;
        else if ( strcmp("uint16", t) == 0 )
            type = config_type_uint16;
        else if ( strcmp("uint32", t) == 0 )
            type = config_type_uint32;
        else if ( strcmp("char", t) == 0 )
            type = config_type_char;
        else if ( strcmp("string", t) == 0 )
            type = config_type_string;
        else
            continue;

        status = config_get_item(b, &item);
        if ( FAILURE(status) )
            continue;

        // Check type
        if ( item->type != type )
            continue;

        switch ( type )
        {
            case config_type_uint8:
                item->value.u8 = atoi(c);
                break;
            case config_type_uint16:
                item->value.u16 = atoi(c);
                break;
            case config_type_uint32:
                item->value.u32 = atoi(c);
                break;
            case config_type_int:
                item->value.i = atoi(c);
                break;
            case config_type_char:
                item->value.c = *c;
                break;
            case config_type_string:
                length = strlen(c);
                item->value.s = malloc(length*sizeof(*item->value.s));
                snprintf(item->value.s, length, "%s", c);
                break;
        }
    }

    return err_OK;
}

/**
 * Save the configuration.
 *
 * @ingroup config
 * @return                  Success status
 */
static error_code config_save(void* data)
{
    list_iterator_t iterator;
    struct config_item* item;

    char    buffer[256];
    FILE*   file;

    file = fopen(CONFIG_FILE, "w");

    if ( file == NULL ) {
//        perror("Failed to open file");
        return err_FILE;
    }

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(this.list, &iterator);
    list_set_iterator_first(iterator);

    while( SUCCESS( list_get_iterator_data(iterator, (void*)&item) ) ) {
        switch ( item->type )
        {
            case config_type_uint8:
                snprintf(buffer, sizeof(buffer), "%s: uint8: %u\n", item->name, item->value.u8);
                break;
            case config_type_uint16:
                snprintf(buffer, sizeof(buffer), "%s: uint16: %u\n", item->name, item->value.u16);
                break;
            case config_type_uint32:
                snprintf(buffer, sizeof(buffer), "%s: uint32: %u\n", item->name, item->value.u32);
                break;
            case config_type_int:
                snprintf(buffer, sizeof(buffer), "%s: int: %d\n", item->name, item->value.i);
                break;
            case config_type_char:
                snprintf(buffer, sizeof(buffer), "%s: char: %c\n", item->name, item->value.c);
                break;
            case config_type_string:
                snprintf(buffer, sizeof(buffer), "%s: string: %s\n", item->name, item->value.s);
                break;
            default:
                buffer[0] = '\0';
                break;
        }

        if ( strlen(buffer) > 0 )
            fwrite(buffer, sizeof(buffer[0]), strlen(buffer), file);

        list_move_iterator_next(iterator);
    }

    pthread_mutex_unlock(&this.list_mutex);

    fclose(file);

    printf("Configuration saved\n");

    return err_OK;
}

/**
 * Get a config item.
 *
 * @ingroup config
 *
 * @param[in]   name    Name of the config item to retrieve
 * @param[out]  item    Returned value
 *
 * @return              Success status
 */
error_code config_get_item(char* name, struct config_item** item)
{
    list_iterator_t iterator;
    error_code status;

    list_create_iterator(this.list, &iterator);

    pthread_mutex_lock(&this.list_mutex);
    list_set_iterator_first(iterator);

    list_find_item(iterator, name, compare_name);
    pthread_mutex_unlock(&this.list_mutex);

    status = list_get_iterator_data(iterator, (void*)item);
    list_destroy_iterator(iterator);

    if( status != err_OK )
        return err_NO_ITEM;

    return err_OK;
}


/**
 * Create a new config item
 *
 * @ingroup config
 *
 * @param[in]   name    Name of the config item to create
 * @param[out]  item    Created item
 *
 * @return              Success status
 */
error_code config_create_item(char* name, enum config_type type, struct config_item** item)
{
    struct config_item* new_item;
    error_code status;

    if ( item == NULL )
        return err_WRONG_ARGUMENT;

    // Look for an existing item
    status = config_get_item(name, item);

    if ( SUCCESS(status) ) {
        if ( (*item)->type != type )
            return err_WRONG_TYPE;
        else
            return err_OK;
    }

    // no existing item, create a new
    new_item = malloc(sizeof(*new_item));

    if ( new_item == NULL )
        return err_OUT_OF_MEMORY;

    new_item->presistent = 0;
    new_item->type = type;

    snprintf(new_item->name, sizeof(new_item->name), "%s", name);
    memset(&new_item->value, 0, sizeof(new_item->value));

    pthread_mutex_lock(&this.list_mutex);
    list_add_last(this.list, new_item);
    pthread_mutex_unlock(&this.list_mutex);

    *item = new_item;

    return err_OK;
}

static error_code config_list_items( void )
{
    list_iterator_t iterator;
    struct config_item* item;

    pthread_mutex_lock(&this.list_mutex);

    list_create_iterator(this.list, &iterator);

    list_set_iterator_first(iterator);

    while( SUCCESS( list_get_iterator_data(iterator, (void*)&item) ) ) {
        switch ( item->type )
        {
            case config_type_uint8:
                printf("%s: %u\n", item->name, item->value.u8);
                break;
            case config_type_uint16:
                printf("%s: %u\n", item->name, item->value.u16);
                break;
            case config_type_uint32:
                printf("%s: %u\n", item->name, item->value.u32);
                break;
            case config_type_int:
                printf("%s: %d\n", item->name, item->value.i);
                break;
            case config_type_char:
                printf("%s: %c\n", item->name, item->value.c);
                break;
            case config_type_string:
                printf("%s: %s\n", item->name, item->value.s);
                break;
            default:
                printf("%s: unknown type\n", item->name);
                break;
        }
        list_move_iterator_next(iterator);
    }

    pthread_mutex_unlock(&this.list_mutex);

    return err_OK;
}

static error_code config_set_value(char* args)
{
    char*       c;
    char*       name;
    error_code  status;
    int         length;
    struct config_item*     item;

    name = args;
    c = strchr(name, ' ');
    if ( c == NULL )
        return err_NO_VALUE;

    *c = '\0';
    c++;

    status = config_get_item(name, &item);

    if ( FAILURE( status ) )
        return err_ITEM_NOT_FOUND;

    switch ( item->type )
    {
        case config_type_uint8:
            item->value.u8 = atoi(c);
            break;
        case config_type_uint16:
            item->value.u16 = atoi(c);
            break;
        case config_type_uint32:
            item->value.u32 = atoi(c);
            break;
        case config_type_int:
            item->value.i = atoi(c);
            break;
        case config_type_char:
            item->value.c = *c;
            break;
        case config_type_string:
            free(item->value.s);
            length = strlen(c);
            item->value.s = malloc(length*sizeof(*item->value.s));
            snprintf(item->value.s, length, "%s", c);
            break;
    }

    return err_OK;
}

/**
 * The command <b>config</b>, configuration.
 *
 * @ingroup config
 *
 * @param[in] args      Arguments
 *
 * @return              Success status
 */
static int command_config(char* args)
{
    char* c;

    c = strchr(args, ' ');
    if ( c != NULL ) {
        *c = '\0';
        c++;
    }

    if ( strcmp("list", args) == 0 ) {
        config_list_items();
    } else if ( strcmp("save", args) == 0 ) {
        modules_run_phase(phase_SAVE_CONFIG);
    } else if ( strcmp("load", args) == 0 ) {
        modules_run_phase(phase_LOAD_CONFIG);
    } else if ( strcmp("default", args) == 0 ) {
        modules_run_phase(phase_LOAD_DEFAULT_VAULES);
    } else if ( strcmp("set", args) == 0 ) {
        config_set_value(c);
    } else {
        printf("Valid arguments: list, save, load, default, set\n");
    }

    return 0;
}
