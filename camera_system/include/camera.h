#ifndef CAMERA_H
#define CAMERA_H

#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <errno.h>
#include <fcntl.h> /* low-level i/o */
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

#include <linux/videodev2.h>

#include "key_tree.h"
#include "utils.h"

#define CLEAR(x) memset(&(x), 0, sizeof(x))

struct buffer {
  void *start;
  size_t length;
};

void errno_exit(const char *s);

int xioctl(int fh, int request, void *arg);

void open_device(char *dev_name, int *fd);
void init_device(char *dev_name, int *fd, int frame_width, int frame_rate,
                 int frame_height, struct buffer **buffers,
                 unsigned int *n_buffers);

void start_capturing(int *fd, unsigned int n_buffers);
void mainloop(int *fd, bool *recording, unsigned int n_buffers,
              struct buffer **buffers, char *upload_url, key_tree *tree,
              unsigned char *previous_hash, unsigned char *current_hash,
              EVP_PKEY *pkey, setup_ret_args_t *);

void stop_capturing(int *fd);

void close_device(int *fd);

void init_mmap(char *dev_name, int *fd, struct buffer **buffers,
               unsigned int *n_buffers);

void uninit_device(unsigned int n_buffers, struct buffer **buffers);

int read_frame(int *fd, unsigned int n_buffers, struct buffer **buffers,
               bool *recording, char *upload_url, key_tree *tree,
               unsigned char *previous_hash, unsigned char *current_hash,
               EVP_PKEY *pkey, setup_ret_args_t *);

void process_image(const void *p, int size, bool *recording, char *upload_url,
                   key_tree *tree, unsigned char *previous_hash,
                   unsigned char *current_hash, EVP_PKEY *pkey);
#endif