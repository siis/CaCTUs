#include <utils.h>

int load_config(const char *filename, config_t *cfg) {
  config_init(cfg);
  config_set_options(cfg,
                     (CONFIG_OPTION_FSYNC | CONFIG_OPTION_SEMICOLON_SEPARATORS |
                      CONFIG_OPTION_COLON_ASSIGNMENT_FOR_GROUPS |
                      CONFIG_OPTION_OPEN_BRACE_ON_SEPARATE_LINE));

  /* Read the file. If there is an error, report it and exit. */
  if (!config_read_file(cfg, filename)) {
    fprintf(stderr, "%s:%d - %s\n", config_error_file(cfg),
            config_error_line(cfg), config_error_text(cfg));
    config_destroy(cfg);
    return (EXIT_FAILURE);
  }

  return (EXIT_SUCCESS);
}

int save_config(config_t *cfg, const char *filename) {
  /* Write out the updated configuration. */
  if (!config_write_file(cfg, filename)) {
    fprintf(stderr, "Error while writing file.\n");
    config_destroy(cfg);
    return (EXIT_FAILURE);
  }

  return (EXIT_SUCCESS);
}

void deserialize_config(config_t *cfg, struct CameraConfig *cam,
                        struct SystemConfig *sys) {
  if (cam) {
    cam->resolution_width =
        config_setting_get_int(config_lookup(cfg, "camera.resolution_width"));
    cam->resolution_height =
        config_setting_get_int(config_lookup(cfg, "camera.resolution_height"));
    cam->framerate =
        config_setting_get_int(config_lookup(cfg, "camera.framerate"));
  }

  if (sys) {
    sys->already_configured = config_setting_get_bool(
        config_lookup(cfg, "system.already_configured"));
    sys->depth_key_tree =
        config_setting_get_int(config_lookup(cfg, "system.depth_key_tree"));
    sys->key_rotation_time =
        config_setting_get_int(config_lookup(cfg, "system.key_rotation_time"));

    read_string_from_config(cfg, &(sys->upload_url), "system.upload_url");

    sys->seed_leaf_time_inf = (float)0;
    sys->seed_leaf_time_inf = config_setting_get_float(
        config_lookup(cfg, "system.seed_leaf_time_inf"));

    sys->seed_key = NULL;
    read_key_from_config(cfg, &(sys->seed_key), "system.seed_key");

    sys->factory_private_key_camera = NULL;
    read_string_from_config(cfg, &(sys->factory_private_key_camera),
                            "system.factory_private_key_camera");

    sys->factory_public_key_camera = NULL;
    read_string_from_config(cfg, &(sys->factory_public_key_camera),
                            "system.factory_public_key_camera");

    sys->private_key_camera = NULL;
    read_string_from_config(cfg, &(sys->private_key_camera),
                            "system.private_key_camera");

    sys->public_key_camera = NULL;
    read_string_from_config(cfg, &(sys->public_key_camera),
                            "system.public_key_camera");

    sys->public_key_main_user = NULL;
    read_string_from_config(cfg, &(sys->public_key_main_user),
                            "system.public_key_main_user");

    sys->recovery_escrow_material = NULL;
    read_string_from_config(cfg, &(sys->recovery_escrow_material),
                            "system.recovery_escrow_material");
  }
}

void serialize_config(config_t *cfg, struct CameraConfig *cam,
                      struct SystemConfig *sys) {

  if (cam) {
    config_setting_set_int(config_lookup(cfg, "camera.resolution_width"),
                           cam->resolution_width);
    config_setting_set_int(config_lookup(cfg, "camera.resolution_height"),
                           cam->resolution_height);
    config_setting_set_int(config_lookup(cfg, "camera.framerate"),
                           cam->framerate);
  }

  if (sys) {
    config_setting_set_bool(config_lookup(cfg, "system.already_configured"),
                            sys->already_configured);
    config_setting_set_int(config_lookup(cfg, "system.depth_key_tree"),
                           sys->depth_key_tree);
    config_setting_set_int(config_lookup(cfg, "system.key_rotation_time"),
                           sys->key_rotation_time);
    config_setting_set_string(config_lookup(cfg, "system.upload_url"),
                              sys->upload_url);

    config_setting_set_float(config_lookup(cfg, "system.seed_leaf_time_inf"),
                             sys->seed_leaf_time_inf);

    write_key_to_config(cfg, sys->seed_key, "system.seed_key");

    config_setting_set_string(
        config_lookup(cfg, "system.factory_private_key_camera"),
        sys->factory_private_key_camera);
    config_setting_set_string(
        config_lookup(cfg, "system.factory_public_key_camera"),
        sys->factory_public_key_camera);
    config_setting_set_string(config_lookup(cfg, "system.private_key_camera"),
                              sys->private_key_camera);
    config_setting_set_string(config_lookup(cfg, "system.public_key_camera"),
                              sys->public_key_camera);
    config_setting_set_string(config_lookup(cfg, "system.public_key_main_user"),
                              sys->public_key_main_user);
    config_setting_set_string(
        config_lookup(cfg, "system.recovery_escrow_material"),
        sys->recovery_escrow_material);
  }
}

void camera_and_system_config_free(struct CameraConfig *camera_config,
                                   struct SystemConfig *system_config) {
  free(camera_config);
  if (system_config->already_configured) {
    free(system_config->seed_key);
  }
  free(system_config);
}

void read_key_from_config(config_t *cfg, unsigned char **output,
                          const char *setting_name) {
  config_setting_t *string;
  string = config_lookup(cfg, setting_name);
  if (string != NULL) {
    int count = config_setting_length(string);

    *output = (unsigned char *)calloc(1, count + 1);
    for (int i = 0; i < count; ++i) {
      (*output)[i] = config_setting_get_int_elem(string, i);
    }
  }
}

void write_key_to_config(config_t *cfg, unsigned char *input,
                         const char *setting_name) {
  config_setting_t *string;
  string = config_lookup(cfg, setting_name);
  if (string != NULL) {
    int count = strlen(input);

    for (int i = 0; i < count; ++i) {
      config_setting_set_int_elem(string, i, input[i]);
    }
  }
}

void read_string_from_config(config_t *cfg, char **output,
                             const char *setting_name) {
  // libconfig library frees the returned string by itself,
  // but this is weird if the rest of the struct does not work similarly so we
  // copy it.
  const char *temp_string;
  temp_string = config_setting_get_string(config_lookup(cfg, setting_name));
  (*output) = (char *)malloc(strlen(temp_string) + 1);
  strncpy((*output), temp_string, strlen(temp_string) + 1);
}

unsigned long long get_current_time_in_milliseconds() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return (unsigned long long)(tv.tv_sec) * 1000 +
         (unsigned long long)(tv.tv_usec) / 1000;
}

bool extract_substring_from_to(unsigned char *string, int from, int to,
                               unsigned char *substring) {
  int length = strlen(string);
  int sub_length = strlen(substring);

  if (from < 0 || from > length) {
    return false;
  }
  if (to < 0 || to < from || to > length) {
    return false;
  }
  if (sub_length < to - from) {
    return false;
  }
  for (int i = from, j = 0; i < to; i++, j++) {
    substring[j] = string[i];
  }
  return true;
}