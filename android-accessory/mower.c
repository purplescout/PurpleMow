#include "commands.h"
int mower_parse(char * inBuffer, char * outBuffer, int length)
{
    switch(inBuffer[0])
    {
    case CMD_READ: 
        outBuffer[0]=0x1;
        outBuffer[1]=inBuffer[1];
        //outBuffer[2]=rand() %9;
        //outBuffer[3]=rand() %9;
        if(inBuffer[1] == CMD_ALL_SENSORS)
        {
            outBuffer[2]=0;
            outBuffer[3]=0;
            outBuffer[4]=0;
            outBuffer[5]=0;
            outBuffer[6]=3;
            outBuffer[7]=255;
            outBuffer[8]=0;
            outBuffer[9]=0;
            outBuffer[10]=0;
            outBuffer[11]=0;
            outBuffer[12]=255;
            outBuffer[13]=255;
            return 14;
        }
        else
        {
            return 4;
        }
        break;
    default:
        outBuffer[0]=inBuffer[0];
        outBuffer[1]=inBuffer[1];
        outBuffer[2]=inBuffer[2];
	outBuffer[3]=inBuffer[3];
        return 0;
        break;
    }	
}



