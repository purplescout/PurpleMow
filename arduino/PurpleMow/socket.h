

int create_server_socket(char * path, int len);
int open_client_socket(char * path,int len);

int write_to_socket(char * message, int len);
int read_from_socket(char * buffer, int len);
int close_socket();
