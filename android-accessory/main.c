
#include <stdio.h>
#include <libusb.h>
#include <usb.h>
#include <string.h>
#include <stdlib.h>

#include "mower.h"
#include "usb_functions.h"
#include "commands.h"

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

#define IN 0x81
#define OUT 0x02

//Nexus 7: 18d1:4e43 (non-debug mode)


#define VENDORID 0x18D1
#define PRODUCTID 0x4E43

#define ACCESSORY_PID 0x2D00

char * app_developer="PurpleScout AB";
char * app_name="PurpleMow";
char * app_description="PurpleScouts Purplemow controller app";
char * app_version="1.0";
char * app_url="http://www.purplescout.se/purplemow";
char * app_serialnumber="1234567890";

void run(void);	

static struct libusb_device_handle * handle;	

	/* Init
	 * Look for the vendor and product id with lsusb and change the 
	 * define above 
	 */
void initialize()
{
	handle=NULL;
	while(handle == NULL)
	{
		printf("Looking for USB device with Vendor:Product id: %X:%X\n\r", VENDORID, PRODUCTID);

		handle=libusb_open_device_with_vid_pid(NULL,VENDORID,PRODUCTID);
		sleep(1);
	}
}

int setup_accessory()
{
	char buffer[2];
	int response=0;
	printf("Starting accessory setup\n\r");
	response = libusb_control_transfer(handle, 0xC0,51,0,0,buffer,strlen(buffer),0);	
	if(response < 0)
		return response;

	usleep(1000);	
	printf("Sending developer \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,0,app_developer,strlen(app_developer),0);
	if(response < 0)
		return response;
	
	printf("Sending name \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,1, app_name,strlen(app_name),0);
	if(response < 0)
		return response;
	printf("Sending description \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,2, app_description,strlen(app_description),0);
	if(response < 0)
		return response;
	printf("Sending version \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,3, app_version,strlen(app_version),0);
	if(response < 0)
		return response;
	printf("Sending url \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,4, app_url,strlen(app_url),0);
	if(response < 0)
		return response;
	printf("Sending serialnumber \n\r");
	response = libusb_control_transfer(handle, 0x40,52,0,5, app_serialnumber,strlen(app_serialnumber),0);
	if(response < 0)
		return response;

	printf("Finalizing \n\r");
	response = libusb_control_transfer(handle,0x40,53,0,0,NULL,0,0);
	return response;
}

int send_data(char * buffer, int length, int * outLength)
{
	printf("Sending buffer: %d %d %d %d, length %d\n\r", buffer[0], buffer[1], buffer[2], buffer[3], length); 
	return libusb_bulk_transfer(handle,OUT,buffer,length,outLength,100);
}

int receive_data(char * buffer, int length, int * outLength)
{
	printf("Receiving %d bytes\n\r", length);
	return libusb_bulk_transfer(handle,IN,buffer,length,outLength,100);
}



int main(int argc, char * argv[])
{

	/*
	USB_props properties;
	properties.vendor=0;
	properties.product=0;
	properties.in=-1;
	properties.out=-1;
	device_info(&properties);
        
	//printf("%04x %04x %02x %02x",properties.vendor, properties.product, properties.in, properties.out);
	exit( 0);*/

	//char buffer[256];
	//int bufferlength=256;
	////int transferred_chars=0;
	int response;


	handle=NULL;
	printf("Initializing\n\r");	
	libusb_init(NULL);

	initialize();

	if(handle == NULL)
		return;	

	libusb_claim_interface(handle,0);	

	/* Set the device to accessory mode */

	
	if( ( response = setup_accessory() ) < 0)
	{
	  printf("Accessory setup failed: %d ", response);
	  return;
	}
	


	/* Connect to the application */
	if( handle != NULL)
  	  libusb_release_interface(handle,0);
	handle=NULL;
	
	
	
	while (handle == NULL)
	{
		printf("Waiting for Vendor id: %X, Product id: %X\n\r", VENDORID, ACCESSORY_PID);
		handle=libusb_open_device_with_vid_pid(NULL,VENDORID,ACCESSORY_PID);
		sleep(1);
	}

	if(libusb_claim_interface(handle,0) != 0)
	{
		printf("Error claiming interface");
		return;
        }
	
	printf("Entering command loop\n\r");	

	run();
}


/*
int mower_parse(char * inBuffer, char * outBuffer, int length)
{
	switch(inBuffer[0])
	{
		case CMD_READ: 
			outBuffer[0]=0x1;
			outBuffer[1]=inBuffer[1];
			outBuffer[2]=rand() %9;
			outBuffer[3]=rand() %9;
			return 0;
			break;
		default: outBuffer[0] = 0x1;
			outBuffer[1]=inBuffer[1];
			outBuffer[2]=rand() %9;
			outBuffer[3]=rand() %9;
			break; 
		default:
			return 0;
			break;
	}	
}
*/



void run(void)
{
    char recvbuffer[4];
    char sendbuffer[4];
    char command[256];
    int bufferlength=4;
    int transferred_chars=0;
    int response;
    int reply;
    libusb_set_debug(NULL,3);



    while( 1)
    {
        response = receive_data(recvbuffer,bufferlength,&transferred_chars);
	//printf("Receive %d buffer: %d %d %d %d\n\r", response, recvbuffer[0], recvbuffer[1], recvbuffer[2], recvbuffer[3]); 
	
	if(response == 0)
	{	
	    snprintf(command,256, "./command.sh %d %d %d %d", recvbuffer[0], recvbuffer[1], recvbuffer[2], recvbuffer[3]);
	    system(command);
	}
	else
	{
	    printf("Error: %s\n", libusb_error_name(response));
	}
		
	if(mower_parse(recvbuffer,sendbuffer,bufferlength) == 0)
	{
	    response = send_data(sendbuffer,bufferlength,&transferred_chars);
	    if(response != 0)
	    printf("Error: %s\n", libusb_error_name(response));
	}
		

    }
}
