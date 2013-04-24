
#include <sys/types.h>
#include <sys/socket.h>

#include <unistd.h>
#include <stdlib.h>
#include <sys/un.h>

#include <stdio.h>

static int SOCK_FD;
static int NEWSOCK_FD;

void error(const char * msg)
{
  perror(msg);
  exit(0);
}



int create_server_socket(char * path, int len)
{
  int servlen, n;
  socklen_t clilen;
  struct sockaddr_un serv_addr;
  SOCK_FD = socket(AF_UNIX,SOCK_STREAM,0);

  	
  if(SOCK_FD < 0)
    error("creating socket");
  
  bzero((char *) &serv_addr, sizeof(serv_addr));

  serv_addr.sun_family = AF_UNIX;
  strcpy(serv_addr.sun_path,path);

  servlen=strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_family);

  if(bind(SOCK_FD,(struct sockaddr *) &serv_addr, servlen) <0)
    error("binding socket");
  
  listen(SOCK_FD,5);
  
}

int open_client_socket(char * path, int len)
{
   int servlen, n;
   struct sockaddr_un serv_addr;

   bzero((char *) &serv_addr, sizeof(serv_addr));
   serv_addr.sun_family=AF_UNIX;

   strcpy(serv_addr.sun_path,path);
   servlen = strlen(serv_addr.sun_path) + sizeof(serv_addr.sun_family);

   SOCK_FD=socket(AF_UNIX, SOCK_STREAM,0);
   if(SOCK_FD==0)
     error("Creating socket");

   if(connect(SOCK_FD,(struct sockaddr *) &serv_addr, servlen) < 0)
     error ("Connecting");

}


int write_to_socket(char * message, int len)
{
    int n;
    char reply[82];
    write(SOCK_FD, message,len);
    n=read(SOCK_FD,reply,80);

    printf("Reply:\n");
    write(1,reply,n);

}

int read_from_socket(char * buffer, int len)
{
   int n,tempsockfd;
   struct sockaddr_un sender_addr;
   
   socklen_t sender_len = sizeof(sender_addr);
   tempsockfd = accept(SOCK_FD, (struct sockaddr *) &sender_addr, &sender_len);
   if(tempsockfd > 0)
   {
      n=read(tempsockfd,buffer,len);
      printf("Msg received: ");
      write(tempsockfd,"OK, received\n",13);
      close(tempsockfd);
      return 0;
   }
   else
   {
     error("Read from socket");
   }
}

int close_socket()
{
   close(SOCK_FD);
}
