/* Include Files */
#include "download.h"
#include "encryption.h"
#include "key_tree.h"
#include <android/log.h>
#include <assert.h>
#include <jni.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
/* Defines */

/* Local Data */

unsigned long long seed_leaf_time_inf = 1600299654000; // ms
char seed_key[32] = {0x67, 0x70, 0xAF, 0xEA, 0xB,  0xCC, 0xE3, 0x33,
                     0x2,  0x67, 0x22, 0x7E, 0xDA, 0x85, 0xBA, 0x5F,
                     0x15, 0xEE, 0xCB, 0xB5, 0x76, 0xFB, 0xB9, 0x4D,
                     0x2D, 0xA3, 0x2A, 0xFC, 0xBE, 0x78, 0x21, 0xE5};

/* Functions */

int launch_download(unsigned long long t1, unsigned long long t2, char *folder,
                    int depth_key_tree, int key_rotation_time) {

  // call to global curl init once per application:
  curl_global_init(CURL_GLOBAL_ALL);

  // Key Tree
  key_leaf *seed_leaf =
      create_key_leaf(0, seed_leaf_time_inf, (void *)seed_key);
  key_tree *tree =
      create_key_tree(depth_key_tree, key_rotation_time, seed_leaf);

  unsigned long long last_time = 1;
  unsigned long long new_last_time = 0;
  int trials = 0;

  char *url = "http://server_ip_or_hostname/cameraX";
  char *url_index = "http://server_ip_or_hostname/indexX.php?t1=";
  char *pubkey_camera =
      "/data/data/com.example.CaCTUs/keys/public_key_camera.pem";
  unsigned char *previous_hash = NULL;
  unsigned char *current_hash = NULL;

  char url_format[200];
  char url_request[300];
  char folder_format[100];
  char buffert1[30];
  char buffert2[30];
  sprintf(buffert1, "%llu", t1);
  sprintf(buffert2, "%llu", t2);
  memset(url_format, '\0', sizeof(url_format));
  memset(folder_format, '\0', sizeof(folder_format));
  memset(url_request, '\0', sizeof(url_request));
  strcpy(url_format, url);
  strcat(url_format, "%llu");
  strcpy(url_request, url_index);
  strcat(url_request, buffert1);
  strcat(url_request, "&t2=");
  strcat(url_request, buffert2);
  strcpy(folder_format, folder);
  strcat(folder_format, "%llu");

  EVP_PKEY *public_key_camera;
  if (load_public_rsa_key_from_file(&public_key_camera, pubkey_camera) ==
      EXIT_FAILURE) {
    return EXIT_FAILURE;
  }

  while (last_time != new_last_time || trials < 60) {
    last_time = new_last_time;
    retrieve_and_parse_html_index(
        url_request, last_time, &new_last_time, tree, &t1, &t2, folder_format,
        url_format, previous_hash, current_hash, public_key_camera);
    if (last_time == new_last_time) {
      sleep(1);
      trials += 1;
    } else {
      trials = 0;
    }
  }

  if (previous_hash) {
    OPENSSL_free(previous_hash);
  }
  if (current_hash) {
    OPENSSL_free(current_hash);
  }

  return (EXIT_SUCCESS);
}

JNIEXPORT jint JNICALL
Java_edu_psu_cse_cactus_DownloadFramesThread_runMainDownloadC(
    JNIEnv *env, jobject obj, jlong t1, jlong t2, jint suffix_frames_folder,
    jint depth_key_tree, jint key_rotation_time) {
  char *folder_base = "/data/data/com.example.CaCTUs/frames/";
  char folder[400];
  char int_buffer[4];
  sprintf(int_buffer, "%d", suffix_frames_folder);
  memset(folder, '\0', sizeof(folder));
  strcpy(folder, folder_base);
  strcat(folder, int_buffer);
  strcat(folder, "/");

  unsigned long long time1 = (unsigned long long)t1;
  unsigned long long time2 = (unsigned long long)t2;

  return launch_download(time1, time2, folder, depth_key_tree,
                         key_rotation_time);
}

JNIEXPORT jint JNICALL Java_edu_psu_cse_cactus_AddUserActivity_generateKeyPair(
    JNIEnv *env, jobject obj) {
  // /* Encryption pair of keys */
  EVP_PKEY *pkey = NULL;
  generate_rsa_key_pair(&pkey);

  if (write_public_rsa_key_to_file(
          pkey,
          "/data/data/com.example.CaCTUs/keys/public_key_this_phone.pem") !=
      EXIT_SUCCESS) {
    return EXIT_FAILURE;
  }
  if (write_private_rsa_key_to_file(
          pkey,
          "/data/data/com.example.CaCTUs/keys/private_key_this_phone.pem") !=
      EXIT_SUCCESS) {
    return EXIT_FAILURE;
  }
  return EXIT_SUCCESS;
}