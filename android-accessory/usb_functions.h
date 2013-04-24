#ifndef USB_FUNCTIONS_H
#define USB_FUNCTIONS_H

struct usb_properties {
	int vendor;
	int product;
	int in;
	int out;
};

typedef struct usb_properties USB_props;

void device_info(USB_props * properties);

#endif
