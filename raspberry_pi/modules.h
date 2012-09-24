#ifndef MODULES_H
#define MODULES_H

#include "error_codes.h"

enum module_phase {
    phase_LOAD_CONFIG,
    phase_SAVE_CONFIG,
    phase_REGISTER_COMMANDS,
    phase_REGISTER_VALUES,
    phase_LOAD_DEFAULT_VAULES,
    phase_START,
    phase_START_SENSORS,
    phase_STOP,
    phase_MOW,
    // used as anchor, must be last in enum
    phase_LAST,
};

error_code modules_init();
error_code modules_run_phase(enum module_phase phase);

error_code module_register_to_phase(enum module_phase phase, error_code(*function)(void* data), void* data);
error_code module_unregister_from_phase(enum module_phase phase, error_code(*function)(void* data));

#endif // MODULES_H
