
CFLAGS=-ggdb -DSIMULATOR
LDFLAGS=-pthread
CXXFLAGS=$(CFLAGS)

# unset VERBOSE to turn on verbosity
VERBOSE=@

CC=gcc

ECHO=echo
RM=rm

OBJ=purplemow.o raspi_io.o auto_management.o dcn.o
BIN=purplemow

Q=$(VERBOSE)
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
	$(Q) $(ECHO) " [ removing $(BIN) ]"
	$(Q) $(RM) -f $(BIN)

