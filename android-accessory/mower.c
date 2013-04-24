#include "commands.h"
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
	/*	default: outBuffer[0] = 0x1;
			outBuffer[1]=inBuffer[1];
			outBuffer[2]=rand() %9;
			outBuffer[3]=rand() %9;
			break; */
		default:
			return 0;
			break;
	}	
}



