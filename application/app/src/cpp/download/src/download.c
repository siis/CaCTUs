#include "download.h"as

// Callback function for CURL to fill Tidy's buffer
size_t write_cb(char *in, size_t size, size_t nmemb, TidyBuffer *out) {
  size_t new_size = size * nmemb;
  tidyBufAppend(out, in, new_size);
  return new_size;
}

// parse HTML index to obtain the href links
void parse(TidyNode node, unsigned long long last_time,
           unsigned long long *new_last_time, key_tree *tree,
           unsigned long long *t1, unsigned long long *t2, char *folder_format,
           char *url_format, EVP_PKEY *pkey) {
  TidyNode child;

  // recursive parsing of all children
  for (child = tidyGetChild(node); child != NULL; child = tidyGetNext(child)) {

    // look for href attribute
    TidyAttr hrefAttr = tidyAttrGetById(child, TidyAttr_HREF);
    if (hrefAttr && tidyAttrValue(hrefAttr)) {
      download_file(tidyAttrValue(hrefAttr), last_time, new_last_time, tree, t1,
                    t2, folder_format, url_format, pkey);
    }

    // recursive call to traverse the tree
    parse(child, last_time, new_last_time, tree, t1, t2, folder_format,
          url_format, pkey);
  }
}

// get content of a website and store it in a buffer
int retrieve_and_parse_html_index(char *url, unsigned long long last_time,
                                  unsigned long long *new_last_time,
                                  key_tree *tree, unsigned long long *t1,
                                  unsigned long long *t2, char *folder_format,
                                  char *url_format,
                                  EVP_PKEY *pkey) {

  // CURL
  CURL *curl_handle;
  char curl_errbuf[CURL_ERROR_SIZE];


  curl_handle = curl_easy_init();                  // init session
  curl_easy_setopt(curl_handle, CURLOPT_URL, url); // set url
  curl_easy_setopt(curl_handle, CURLOPT_ERRORBUFFER,
                   curl_errbuf); // set errorbuffer
  curl_easy_setopt(curl_handle, CURLOPT_NOPROGRESS,
                   0L);                               // disable progress meter
  curl_easy_setopt(curl_handle, CURLOPT_VERBOSE, 0L); // debug option
  curl_easy_setopt(curl_handle, CURLOPT_WRITEFUNCTION,
                   write_cb); // callback function

  // Tidy
  TidyDoc index_doc;
  TidyBuffer index_buf = {0};
  TidyBuffer tidy_errbuf = {0};
  int err;

  index_doc = tidyCreate();                        // create doc
  tidyOptSetBool(index_doc, TidyForceOutput, yes); // force output
  tidyOptSetInt(index_doc, TidyWrapLen, 4096);     // max length
  tidySetErrorBuffer(index_doc, &tidy_errbuf);     // set err buf
  tidyBufInit(&index_buf);                         // init buf

  curl_easy_setopt(curl_handle, CURLOPT_WRITEDATA,
                   &index_buf);         // buffer to store data in
  err = curl_easy_perform(curl_handle); // perform curl operation

  if (err == CURLE_OK) {
    tidyParseBuffer(index_doc, &index_buf); // parse with Tidy
    parse(tidyGetBody(index_doc), last_time, new_last_time, tree, t1, t2,
          folder_format, url_format, pkey);
  } else {
    fprintf(stderr, "%s\n", curl_errbuf);
    exit(EXIT_FAILURE);
  }
  // free
  curl_easy_cleanup(curl_handle);
  tidyBufFree(&index_buf);
  tidyBufFree(&tidy_errbuf);
  tidyRelease(index_doc);
  return err;
}

void init_enc_buf(struct enc_buf *buf) {
  buf->len = 0;
  buf->ptr = (unsigned char *)malloc(40000);
  if (buf->ptr == NULL) {
    exit(EXIT_FAILURE);
  }
}

// second callback function to download each frame into a buffer
size_t write_enc_buf(void *ptr, size_t size, size_t nmemb,
                     struct enc_buf *buf) {
  size_t new_len = buf->len + size * nmemb;
  buf->ptr = (unsigned char *)realloc(buf->ptr, new_len);
  if (buf->ptr == NULL) {
    exit(EXIT_FAILURE);
  }
  memcpy(buf->ptr + buf->len, ptr, size * nmemb);
  buf->len = new_len;

  return size * nmemb;
}

int download_file(const char *filename, unsigned long long last_time,
                  unsigned long long *new_last_time, key_tree *tree,
                  unsigned long long *t1, unsigned long long *t2,
                  char *folder_format, char *url_format,
                  EVP_PKEY *pkey) {

  unsigned long long file_time;
  sscanf(filename, "%llu", &file_time);

  char url_file[300];
  char filepath[300];
  // Define URL and path where to store the frames on the phone
  sprintf(url_file, url_format, file_time);
  sprintf(filepath, folder_format, file_time);

  if (file_time > last_time) {
    *new_last_time = file_time;

    struct enc_buf enc_frame;
    init_enc_buf(&enc_frame);

    // CURL request
    CURL *curl_handle;
    char curl_errbuf[CURL_ERROR_SIZE];

    curl_handle = curl_easy_init();                       // init session
    curl_easy_setopt(curl_handle, CURLOPT_URL, url_file); // set url
    curl_easy_setopt(curl_handle, CURLOPT_ERRORBUFFER,
                     curl_errbuf); // set errorbuffer
    curl_easy_setopt(curl_handle, CURLOPT_NOPROGRESS,
                     0L); // disable progress meter
    curl_easy_setopt(curl_handle, CURLOPT_VERBOSE, 0L); // debug option
    curl_easy_setopt(curl_handle, CURLOPT_WRITEFUNCTION,
                     write_enc_buf); // callback function
    curl_easy_setopt(curl_handle, CURLOPT_WRITEDATA,
                     &enc_frame); // buffer to store data in

    int err;
    err = curl_easy_perform(curl_handle); // perform curl operation

    if (err == CURLE_OK) {
      // retrieve decryption key
      unsigned char *key;
      key = NULL;

      if (!find_key_for_timestamp(file_time, tree, false, &key)) {
        exit(EXIT_FAILURE);
      }

      int iv_len = 16;
      unsigned char iv[iv_len];
      int tag_len = 16;
      unsigned char tag[tag_len];
      int sig_len = 256;

      memcpy(iv, enc_frame.ptr + enc_frame.len - iv_len - tag_len - sig_len, iv_len);
      memcpy(tag, enc_frame.ptr + enc_frame.len - tag_len - sig_len, tag_len);

      // Decrypt
      int decrypted_len;
      unsigned char decrypted_text[enc_frame.len - iv_len - tag_len - sig_len];
      decrypted_len = gcm_decrypt(enc_frame.ptr, enc_frame.len - iv_len - tag_len - sig_len, (unsigned char *) &file_time, sizeof(unsigned long long), tag,
                              key,  iv, iv_len, decrypted_text);

      if (decrypted_len < 0){
        __android_log_write(ANDROID_LOG_INFO, "verification tag is", "incorrect");
        exit(EXIT_FAILURE);
      }
      // Verify signature
      unsigned char sig[sig_len];
      if (verify_sign_rsa(tag, tag_len,enc_frame.ptr + enc_frame.len - sig_len, sig_len, pkey) != EXIT_SUCCESS){
          __android_log_write(ANDROID_LOG_INFO, "verification signature is", "incorrect");
          exit(EXIT_FAILURE);
      }

      OPENSSL_cleanse(iv, iv_len);
      OPENSSL_cleanse(tag, tag_len);
      OPENSSL_cleanse(sig, sig_len);
      // write decrypted frame to disk
      umask(002);
      FILE *fp = fopen(filepath, "wb");
      fwrite(decrypted_text, decrypted_len, 1, fp);
      fflush(fp);
      fclose(fp);

      OPENSSL_cleanse(decrypted_text, decrypted_len);


    } else {
      fprintf(stderr, "%s\n", curl_errbuf);
      exit(EXIT_FAILURE);
    }

    // free
    curl_easy_cleanup(curl_handle);
    free(enc_frame.ptr);
  }

  return (EXIT_SUCCESS);
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

unsigned long long get_current_time_in_milliseconds() {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return (unsigned long long)(tv.tv_sec) * 1000 +
         (unsigned long long)(tv.tv_usec) / 1000;
}