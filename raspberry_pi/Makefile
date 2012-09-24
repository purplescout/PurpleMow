
CFLAGS = -ggdb
CFLAGS += -DSIMULATOR
LDFLAGS = -pthread -lrt
CXXFLAGS = $(CFLAGS)

# unset VERBOSE to turn on verbosity
VERBOSE = @

CC_PATH = ~/x-tools7h/arm-unknown-linux-gnueabi/bin
CC_PREFIX = arm-unknown-linux-gnueabi-
#CC = $(CC_PATH)/$(CC_PREFIX)gcc
CC = gcc

ECHO = echo
RM = rm

# select which io to use
# IO_LAYER = io_i2c.c
IO_LAYER = io_net.c
# IO_LAYER = io_gpio.c

purplemow_src = purplemow.c \
				mow.c \
				io.c \
				$(IO_LAYER) \
				auto_management.c \
				cli.c \
				dcn.c \
				messages.c \
				modules.c \
				communicator.c \
				config.c \
				sensors.c \
				sensor_range.c \
				sensor_bwf.c \
				thread.c \
				poller.c \
				utils/utils.c \
				utils/list.c \
				utils/state.c \
				test.c \
				test_thread.c

BIN = purplemow
OBJ = $(purplemow_src:.c=.o)

Q = $(VERBOSE)
MAKEFLAGS += --no-print-directory

all: $(BIN)

$(BIN): $(OBJ)
	$(Q) $(ECHO) " [ LD ] $@"
	$(Q) $(CC) -o $@ $^ $(CFLAGS) $(LDFLAGS)

%.o: %.c
	$(Q) $(ECHO) " [ CC ] $@"
	$(Q) $(CC) -c -o $@ $< $(CFLAGS) $(LDFLAGS)


.PHONY: clean cleanall all

clean:
	$(Q) $(ECHO) " [ removing *.o ]"
	$(Q) $(RM) -f *.o
	$(Q) $(RM) -f utils/*.o
	$(Q) $(ECHO) " [ removing *.d ]"
	$(Q) $(RM) -f *.d
	$(Q) $(RM) -f utils/*.d
	$(Q) $(RM) -f *.d.????
	$(Q) $(RM) -f utils/*.d.????
	$(Q) $(ECHO) " [ removing $(BIN) ]"
	$(Q) $(RM) -f $(BIN)

%.d: %.c
	@set -e; rm -f $@; \
	$(CC) -M $(CXXFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$

-include $(OBJ:.o=.d)
