
#include <string.h>
#include "socket.h"

int main(int argc, char * argv[])
{
	char * path = "/var/run/purplemow.sock";
	open_client_socket( path ,strlen(path));
	write_to_socket(argv[1],strlen( argv[1]));
}


