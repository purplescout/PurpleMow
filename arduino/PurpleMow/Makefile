
all:
	gcc PurpleMow.c socket.c -o arduino-c -I/usr/local/include -L/usr/local/lib -lwiringPi -lpthread
	gcc cli.c socket.c -o mowcli -I/usr/local/include -L/usr/local/lib -lwiringPi -lpthread

clean:
	rm arduino-c
	rm mowcli
