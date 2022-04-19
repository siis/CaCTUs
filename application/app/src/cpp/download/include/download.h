/* Include Files */
#include "encryption.h"
#include <android/log.h>
#include <assert.h>
#include <curl/curl.h>
#include <key_tree.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <tidy.h>
#include <tidybuffio.h>
#include <time.h>
/* Defines */

struct enc_buf {
  unsigned char *ptr;
  size_t len;
};

/* Local Data */
bool PERF;
unsigned long long int time_before_download;
unsigned long long int time_after_download;
unsigned long long int time_after_key_extraction;
unsigned long long int time_after_decryption_and_tag;
unsigned long long int time_after_sign_verification;
unsigned long long time_after_write_to_disk;

/* Functions */

// Callback function for CURL to fill Tidy's buffer
size_t write_cb(char *in, size_t size, size_t nmemb, TidyBuffer *out);

// retrieve and parse HTML index page
int retrieve_and_parse_html_index(char *url, unsigned long long last_time,
                                  unsigned long long *new_last_time,
                                  key_tree *tree, char *folder_format,
                                  char *url_format, EVP_PKEY *pkey);

// parse HTML page to retrieve <a href="link"> links
void parse(TidyNode node, unsigned long long last_time,
           unsigned long long *new_last_time, key_tree *tree, char *folder_format,
           char *url_format, EVP_PKEY *pkey);

// process each link
int download_file(const char *filename, unsigned long long last_time,
                  unsigned long long *new_last_time, key_tree *tree,
                  char *folder_format, char *url_format, EVP_PKEY *pkey);

// utils function
bool extract_substring_from_to(unsigned char *string, int from, int to,
                               unsigned char *substring);

unsigned long long get_current_time_in_milliseconds();
