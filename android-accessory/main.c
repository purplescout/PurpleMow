
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
        response = receive_data(handle, recvbuffer,bufferlength,&transferred_chars);
        //printf("Receive %d buffer: %d %d %d %d\n\r", response, recvbuffer[0], recvbuffer[1], recvbuffer[2], recvbuffer[3]); 

        if(response == 0)
        {
            snprintf(command,256, "./command.sh %d %d %d %d", recvbuffer[0], recvbuffer[1], recvbuffer[2], recvbuffer[3]);
            system(command);
        }
        else
        {
            //printf("Error: %s\n", libusb_error_name(response));
        }

        if(mower_parse(recvbuffer,sendbuffer,bufferlength) == 0)
        {
            response = send_data(handle,sendbuffer,bufferlength,&transferred_chars);
            if(response != 0)
                printf("Error: %s\n", libusb_error_name(response));
        }
    }
}
