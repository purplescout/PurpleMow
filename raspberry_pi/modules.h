#ifndef MODULES_H
#define MODULES_H

#include "error_codes.h"

enum module_phase {
    phase_START,
    phase_START_SENSORS,
    phase_STOP,
    phase_MOW,
};

error_code modules_init();
error_code modules_run_phase(enum module_phase phase);

error_code module_register_to_phase(enum module_phase phase, error_code(*function)());
error_code module_unregister_from_phase(enum module_phase phase, error_code(*function)());

#endif // MODULES_H
