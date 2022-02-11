IDIR =../include
CC=gcc
CFLAGS=-I$(IDIR)

ODIR=obj

LIBS=-lcurl -ltidy -lssl -lcrypto -lm

_DEPS = download.h key_tree.h encryption.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = download.o key_tree.o encryption.o main.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))


$(ODIR)/%.o: %.c $(DEPS)
	$(CC) -g -c -o $@ $< $(CFLAGS)

main: $(OBJ)
	$(CC) -g -o $@ $^ $(CFLAGS) $(LIBS)

.PHONY: clean
clean:
	rm -f $(ODIR)/*.o core img/*