#ifndef PURPLEMOW_H
#define PURPLEMOW_H

#include "error_codes.h"
#include "messages.h"
#include "command.h"

enum decision {
    decision_range_too_close,
    decision_range_ok,
};

// Messages

// sensor_decision
struct message_sensor_decision_body {
    enum sensor         sensor;
    enum decision       decision;
};

struct message_sensor_decision {
    struct message_head                     head;
    struct message_sensor_decision_body     body;
};

// Public functions
error_code main_range_too_close();
error_code main_range_ok();

#endif // PURPLEMOW_H
