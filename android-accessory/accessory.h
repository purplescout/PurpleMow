#include <libusb.h>

/* Should be refactored in some way, both looks for specific ids and returns libusb_device_handles */
struct libusb_device_handle * switch_device();
struct libusb_device_handle * init_accessory();

int setup_accessory(struct libusb_device_handle * aHandle);


int send_data(struct libusb_device_handle * aHandle, char * buffer, int length, int * outLength);

int receive_data(struct libusb_device_handle * aHandle, char * buffer, int length, int * outLength);
