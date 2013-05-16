#include <libusb.h>
#include <stdio.h>
#include <string.h>

char * app_developer="PurpleScout AB";
char * app_name="PurpleMow";
char * app_description="PurpleScouts Purplemow controller app";
char * app_version="1.0";
char * app_url="http://www.purplescout.se/purplemow";
char * app_serialnumber="1234567890";

#define NEXUS_7_VID 0x18D1
#define NEXUS_7_PID 0x4E43

#define NEXUS_S_VID 0x18D1
#define NEXUS_S_PID 0x4E12


struct usb_device {
    uint16_t vid;
    uint16_t pid;
    uint16_t IN;
    uint16_t OUT;
};

static const struct usb_device id_table[] =
{
    { NEXUS_7_VID, NEXUS_7_PID, 0x81, 0x02 },
    { NEXUS_S_VID, NEXUS_S_PID, 0x83, 0x03 }
};

static const struct usb_device accessory_id_table[] =
{
    { 0x18d1, 0x2D00, 0, 0 },
    { 0x18d1, 0x2D00, 0, 0}
};

#define NUMBER_OF_IDS (sizeof(id_table) / sizeof(id_table[0]))
#define NUMBER_OF_ACCESSORY_IDS (sizeof(accessory_id_table) / sizeof(accessory_id_table[0]))

static struct usb_device current_device;

struct libusb_device_handle * switch_device()
{
    static libusb_device_handle * handle=NULL;
    int i;
    while(handle == NULL)
    {
        for(i =0;i<NUMBER_OF_IDS;i++)
        {
            handle = libusb_open_device_with_vid_pid(NULL,id_table[i].vid, id_table[i].pid);
            printf("Looking for %X, %X\n\r", id_table[i].vid,id_table[i].pid);
            if(handle != NULL)
            {
                current_device=id_table[i];
                break;
            }
        }
        sleep(1);
    }
    return handle;
}

struct libusb_device_handle * init_accessory()
{
    static libusb_device_handle * handle = NULL;
    int i;
    while(handle == NULL)
    {
        for(i =0;i<NUMBER_OF_ACCESSORY_IDS;i++)
        {
            handle = libusb_open_device_with_vid_pid(NULL,accessory_id_table[i].vid, accessory_id_table[i].pid);
            printf("Looking for %X, %X\n\r", accessory_id_table[i].vid,accessory_id_table[i].pid);
            if(handle != NULL)
                break;
        }
        sleep(1);
    }
    return handle;
}

int setup_accessory(struct libusb_device_handle * aHandle)
{

    char buffer[2];
    int response=0;
    printf("Starting accessory setup\n\r");
    response = libusb_control_transfer(aHandle, 0xC0,51,0,0,buffer,strlen(buffer),0);
    if(response < 0)
        return response;

    usleep(1000);
    printf("Sending developer \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,0,app_developer,strlen(app_developer),0);
    if(response < 0)
        return response;

    printf("Sending name \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,1, app_name,strlen(app_name),0);
    if(response < 0)
        return response;
    printf("Sending description \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,2, app_description,strlen(app_description),0);
    if(response < 0)
        return response;
    printf("Sending version \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,3, app_version,strlen(app_version),0);
    if(response < 0)
        return response;
    printf("Sending url \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,4, app_url,strlen(app_url),0);
    if(response < 0)
        return response;
    printf("Sending serialnumber \n\r");
    response = libusb_control_transfer(aHandle, 0x40,52,0,5, app_serialnumber,strlen(app_serialnumber),0);
    if(response < 0)
        return response;

    printf("Finalizing \n\r");
    response = libusb_control_transfer(aHandle,0x40,53,0,0,NULL,0,0);
    return response;
}

//Move to accessory.c?
int send_data(struct libusb_device_handle * aHandle, char * buffer, int length, int * outLength)
{
    printf("Sending buffer: %d %d %d %d, length %d\n\r", buffer[0], buffer[1], buffer[2], buffer[3], length); 
    return libusb_bulk_transfer(aHandle,current_device.OUT,buffer,length,outLength,100);
}

//Move to accessory.c?
int receive_data(struct libusb_device_handle * aHandle, char * buffer, int length, int * outLength)
{
//	printf("Receiving %d bytes\n\r", length);
    return libusb_bulk_transfer(aHandle,current_device.IN, buffer,length,outLength,100);
}
