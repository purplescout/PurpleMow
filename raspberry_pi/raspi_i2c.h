#ifndef RASPI_I2C_H
#define RASPI_I2C_H

int purple_io_init();
#ifdef SIMULATOR
int io_test_command_1(int i);
int io_test_command_2(void);
#endif // SIMULATOR

#endif // RASPI_I2C_H
