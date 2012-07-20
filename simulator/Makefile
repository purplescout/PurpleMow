
CFLAGS = -ggdb
#CFLAGS += -DREADABLE_COMMANDS
CFLAGS += $(shell pkg-config --cflags gtk+-2.0)
LDFLAGS = $(shell pkg-config --libs gtk+-2.0)
#LDFLAGS += -pthread -lrt
LDFLAGS += -lm
CXXFLAGS = $(CFLAGS)

# unset VERBOSE to turn on verbosity
VERBOSE = @

CC = gcc

ECHO = echo
RM = rm

purplesim_src = purplesim.c \
                mower_net.c \
                mower_calc.c \
                distance.c \
                table.c \
                sensor_obstacle.c \
                sensor_bwf.c \
                sensor_voltage.c \
                sensor_moisture.c

BIN = purplesim
OBJ = $(purplesim_src:.c=.o)

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
	$(Q) $(ECHO) " [ removing *.d ]"
	$(Q) $(RM) -f *.d
	$(Q) $(RM) -f *.d.????
	$(Q) $(ECHO) " [ removing $(BIN) ]"
	$(Q) $(RM) -f $(BIN)

%.d: %.c
	@set -e; rm -f $@; \
	$(CC) -M $(CXXFLAGS) $< > $@.$$$$; \
	sed 's,\($*\)\.o[ :]*,\1.o $@ : ,g' < $@.$$$$ > $@; \
	rm -f $@.$$$$

-include $(OBJ:.o=.d)
