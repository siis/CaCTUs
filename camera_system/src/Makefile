IDIR =../include
CC=gcc
CFLAGS=-I$(IDIR)

ODIR=obj
ODIR_PERF=obj/perf
LDIR =../lib

LIBS=-lssl -lcrypto -lm -lconfig

_DEPS = utils.h encryption.h key_tree.h camera.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = utils.o encryption.o key_tree.o camera.o main.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))
OBJ_PERF = $(patsubst %,$(ODIR_PERF)/%,$(_OBJ))

$(ODIR)/%.o: %.c $(DEPS)
	$(CC) -g -c -o $@ $< $(CFLAGS)

$(ODIR_PERF)/%.o: %.c $(DEPS)
	$(CC) -DPERF -g -c -o $@ $< $(CFLAGS)

main: $(OBJ)
	$(CC) -g -o $@ $^ $(CFLAGS) $(LIBS)

perf_eval: $(OBJ_PERF)
	$(CC) -g -o $@ $^ $(CFLAGS) $(LIBS)

.PHONY: clean
clean:
	rm -f $(ODIR)/*.o $(ODIR_PERF)/*.o core img/* main perf_eval