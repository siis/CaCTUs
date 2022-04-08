#ifndef UTILS_H
#define UTILS_H

#include <libconfig.h>
#include <math.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <time.h>

typedef enum {
  INVALID = 0,
  SETUP = 1,
  SHARED_USR_RETRIEVE = 2, // retrieve shared user list
  SHARED_USR_UPDATE = 3,   // update shared user list
  ESCROW_RETRIEVE = 4,     // retrieve escrow
  ESCROW_UPDATE = 5        // update escrow
} req_type_t;

typedef struct listener_thr_args_t {
  config_t *config;
  struct SystemConfig *system_config;
} listener_thr_args_t;

typedef struct setup_ret_args_t {
  uint8_t do_setup;
  uint8_t *buf_frame_data;
  uint32_t buf_bytesused;
} setup_ret_args_t;

extern pthread_mutex_t system_config_lock;

struct CameraConfig {
  int resolution_width;
  int resolution_height;
  int framerate;
};

struct SystemConfig {
  bool already_configured;
  int depth_key_tree;
  int key_rotation_time;
  char *upload_url;
  double seed_leaf_time_inf;
  unsigned char *seed_key;
  char *private_key_camera;
  char *public_key_camera;
  char *public_key_main_user;
};

int load_config(const char *filename, config_t *cfg);

int save_config(config_t *cfg, const char *filename);

void deserialize_config(config_t *cfg, struct CameraConfig *cam,
                        struct SystemConfig *sys);

void serialize_config(config_t *cfg, struct CameraConfig *cam,
                      struct SystemConfig *sys);

void camera_and_system_config_free(struct CameraConfig *camera_config,
                                   struct SystemConfig *system_config);

void read_key_from_config(config_t *cfg, unsigned char **output,
                          const char *setting_name);

void read_string_from_config(config_t *cfg, char **output,
                             const char *setting_name);

void write_key_to_config(config_t *cfg, unsigned char *input,
                         const char *setting_name);

unsigned long long get_current_time_in_milliseconds();

bool extract_substring_from_to(unsigned char *string, int from, int to,
                               unsigned char *substring);

#endif