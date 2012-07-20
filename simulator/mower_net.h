#ifndef MOWER_NET_H
#define MOWER_NET_H

#define DEFAULT_PORT 35424

void mower_net(char* port);
void mower_set_motors(int right_motor_inverted);
void mower_net_debug(int debug_commands, int debug_values);

#endif // MOWER_NET_H
