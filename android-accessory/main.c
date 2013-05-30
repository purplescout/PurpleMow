
#include <stdio.h>
#include <libusb.h>
#include <usb.h>
#include <string.h>
#include <stdlib.h>

#include "accessory.h"
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


void run(void);	

static struct libusb_device_handle * handle;

int main(int argc, char * argv[])
{

    //Start with the device in an unknown state, if no device pids are found
    //Check for accessory pids
    int response;

    handle=NULL;
    printf("Initializing\n\r");	
    libusb_init(NULL);

    handle=switch_device();

    if(&handle == NULL)
    {
        printf("No handle, exiting\n\r");
        return;	
    }

    libusb_claim_interface(handle,0);	

    /* Set the device to accessory mode */


    printf("Setting up device\n\r");	
    if( ( response = setup_accessory(handle) ) < 0)
    {
        printf("Accessory setup failed: %d ", response);
        return;
    }



    /* Connect to the application */
    if( handle != NULL)
        libusb_release_interface(handle,0);
    handle=NULL;


    handle=init_accessory();

    if(libusb_claim_interface(handle,0) != 0)
    {
        printf("Error claiming interface");
        return;
    }

    printf("Entering command loop\n\r");

    run();
}


void run(void)
{
    uint8_t recvbuffer[4];
    uint8_t sendbuffer[12];
    uint8_t command[256];
    int bufferlength=3;
    int transferred_chars=0;
    int response;
    int reply;
    int to_write;
    libusb_set_debug(NULL,3);


    /* Exit loop on LIBUSB_ERROR_NO_DEVICE */
    while( response != -4)
    {
        response = receive_data(handle, recvbuffer,bufferlength,&transferred_chars);
        //printf("Receive %d buffer: %d %d %d %d\n\r", response, recvbuffer[0], recvbuffer[1], recvbuffer[2], recvbuffer[3]); 

        if(response == 0)
        {
            if(recvbuffer[0] != 4)// && recvbuffer[1] == 2)
            {
		printf("Cmd received %u %u %u\n", recvbuffer[0], recvbuffer[1], recvbuffer[2]);
                fflush(stdout);
            }
            //snprintf(command,256, "./command.sh %d %d %d", recvbuffer[0], recvbuffer[1], recvbuffer[2]);
            //system(command);
        }
        else
        {
            printf("Error: %s\n", libusb_error_name(response));
            if(response == -7)
            {
                
                printf("Bytes received %d\n", transferred_chars);
            }
        }

        to_write=mower_parse(recvbuffer,sendbuffer,bufferlength);
        if(to_write > 0)
        {
            response = send_data(handle,sendbuffer,to_write,&transferred_chars);
            if(response != 0)
                printf("Error: %s\n", libusb_error_name(response));
            if(response == -7)
            {
                
                printf("Bytes received %d\n", transferred_chars);
            }
        }
    }

    libusb_close(handle);
    libusb_exit(NULL);
}
