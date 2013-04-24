

//Used by libusb when sending/receiving data
/* TODO: Read IN and OUT endpoint address 
 * from Interface descriptor -> Endpoint descriptor
 *
 * Lista devices pa usb hubben
 * kolla IN och OUT addresser foer en specific bus + device
 * tex bus 001 device 021
 * Foerutsaett att det aer en android device
 * Kika pa kaellkod till lsusb
 */


/* Get device VENDORID PRODUCTID, IN and OUT addres from USB path */

#include <stdio.h>
#include <libusb.h>

#include "usb_functions.h"

#define BUSNUMBER 2
#define DEVICENUMBER 10



void device_info(struct usb_properties * properties)
{
	int i,j,k,l,m;

	struct libusb_device_descriptor desc;

	struct libusb_config_descriptor * config;

	int num_devices=0;
	int err=0, status=0;

	libusb_context * ctx;
	libusb_device ** list;

	err=libusb_init(&ctx);
	
	num_devices=libusb_get_device_list(ctx, &list);
	
	for(i=0; i < num_devices; i++)
	{
		libusb_device * dev = list[i];
		uint8_t bnum = libusb_get_bus_number(dev);
		uint8_t dnum = libusb_get_device_address(dev);
		if(BUSNUMBER != bnum || DEVICENUMBER != dnum)
			continue;

		libusb_get_device_descriptor(dev, &desc);

		printf("ID: %04x:%04x\n",desc.idVendor, desc.idProduct);

		properties->vendor=desc.idVendor;
		properties->product=desc.idProduct;

		//Get interface descriptor
		for(k =0;k<desc.bNumConfigurations;k++)
		{
			libusb_get_config_descriptor(dev, j,&config);
			printf("Config\n\r");

			for(k=0; k < config->bNumInterfaces; k++)
			{
				printf("Interfaces\n\r");
				
				for(l=0; l<config->interface[k].num_altsetting;l++)
				{
					for(m=0; m< config->interface[k].altsetting[l].bNumEndpoints;m++)
					{
						int eaddress= config->interface[k].altsetting[l].endpoint[m].bEndpointAddress;
						printf("Endpoint: %02x\n\r",eaddress);
						/*			
						if(eaddress & 0x80)
						{
							properties->out=eaddress;
						}
						else
						{
							properties->in=eaddress;
						} */
					}	
				}
			}
			libusb_free_config_descriptor(config);
		}
	}


}
