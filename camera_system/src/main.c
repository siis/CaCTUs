/* Include Files */
#include <assert.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <sys/socket.h>
#include <unistd.h>

/* Project Include Files */
#include "camera.h"
#include "encryption.h"
#include "key_tree.h"
#include "utils.h"

/* Local Data */
const char *config_filename = "config.cfg";

int main(void) {

  // Load Configurations from File to know state of the system
  config_t *config = (config_t *)malloc(sizeof(config_t));

  if (load_config(config_filename, config) == EXIT_FAILURE) {
    free(config);
    return (EXIT_FAILURE);
  }

  struct CameraConfig *camera_config;
  camera_config = (struct CameraConfig *)malloc(sizeof(struct CameraConfig));
  struct SystemConfig *system_config;
  system_config = (struct SystemConfig *)malloc(sizeof(struct SystemConfig));

  deserialize_config(config, camera_config, system_config);

  /* initialize key tree */
  key_leaf *seed_leaf = create_key_leaf(0, system_config->seed_leaf_time_inf,
                                        system_config->seed_key);
  key_tree *tree = create_key_tree(system_config->depth_key_tree,
                                   system_config->key_rotation_time, seed_leaf);

  /* LOAD CAMERA AND MAIN USER KEYS */
  EVP_PKEY *private_key_camera = NULL;
  EVP_PKEY *public_key_main_user = NULL;

  int pub_result = load_public_rsa_key_from_file(
      &public_key_main_user, system_config->public_key_main_user);

  int priv_result = load_private_rsa_key_from_file(
      &private_key_camera, system_config->private_key_camera);

  if (pub_result == EXIT_FAILURE || priv_result == EXIT_FAILURE) {
    // destroy config struct
    config_destroy(config);
    free(config);
    camera_and_system_config_free(camera_config, system_config);
    exit(EXIT_FAILURE);
  }

  /* START RECORDING AND PROCESSING FRAMES */

  char *dev_name;
  int fd = -1;
  struct buffer *buffers;
  unsigned int n_buffers;
  bool recording = true;
  dev_name = "/dev/video0";

  open_device(dev_name, &fd);

  init_device(dev_name, &fd, camera_config->resolution_width,
              camera_config->resolution_height, camera_config->framerate,
              &buffers, &n_buffers);

  start_capturing(&fd, n_buffers);

  mainloop(&fd, &recording, n_buffers, &buffers, system_config->upload_url,
           tree, private_key_camera, NULL);

  stop_capturing(&fd);

  uninit_device(n_buffers, &buffers);

  close_device(&fd);

  // destroy config struct
  config_destroy(config);
  free(config);
  camera_and_system_config_free(camera_config, system_config);

  return (EXIT_SUCCESS);
}
