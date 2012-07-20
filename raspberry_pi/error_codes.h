#ifndef ERROR_CODES_H
#define ERROR_CODES_H

typedef enum error_code {
    err_OK,
    err_NOT_IMPLEMENTED,
    err_WRONG_ARGUMENT,
    err_NOT_INITIALIZED,
    err_ALREADY_INITIALIZED,
    err_BUFFER_TOO_SMALL,
    err_OPEN_DEVICE,
    err_CONFIGURE_DEVICE,
    err_SOCKET,
    err_THREAD,
    err_OUT_OF_MEMORY,
    err_ALREADY_REGISTERED,
    err_NOT_REGISTERED,
    err_UNKNOWN_COMMAND,
    err_MESSAGE,
    err_UNHANDLED_SENSOR,
    err_FILE,
    err_NO_ITEM,
    err_EMPTY_LIST,
    err_ALREADY_IN_LIST,
    err_NO_MORE_ITEMS,
    err_LESS_THAN,
    err_GREATER_THAN,
    err_EQUAL,
    err_NOT_EQUAL,
    err_NO_HANDLER,
    err_NETWORK,
} error_code;

#define SUCCESS(x) (x) == err_OK
#define FAILURE(x) (x) != err_OK

#endif // ERROR_CODES_H
