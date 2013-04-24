
#include <stdio.h>
#include <string.h>
#include "socket.h"

int main(int argc, char * argv[])
{
	
	char command[20];
	char * path = "/var/run/purplemow.sock";
	if(argc < 5)
	{
	    printf("Must provide 4 values, pad with 0 if needed");
	}
	open_client_socket( path ,strlen(path));

	
	snprintf(command,20,"%s %s %s %s",argv[1],argv[2], argv[3], argv[4]);
	write_to_socket(command,20);
	printf("Sent values: %s\n", command);
}


