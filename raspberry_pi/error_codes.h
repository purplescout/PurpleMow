#ifndef ERROR_CODES_H
#define ERROR_CODES_H

typedef enum error_code {
    err_OK,
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
} error_code;

#define SUCCESS(x) (x) == err_OK
#define FAILURE(x) (x) != err_OK

#endif // ERROR_CODES_H