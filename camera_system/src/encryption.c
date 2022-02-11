/* Include Files */

/* Project Include Files */
#include "encryption.h"

/* Defines */

/* Local Data */

/* Functions */

void generate_rsa_key_pair(EVP_PKEY **pkey) {
  EVP_PKEY_CTX *ctx;

  if (!(ctx = EVP_PKEY_CTX_new_id(EVP_PKEY_RSA, NULL)))
    handleErrors();

  if (EVP_PKEY_keygen_init(ctx) <= 0)
    handleErrors();

  if (EVP_PKEY_CTX_set_rsa_keygen_bits(ctx, 2048) <= 0)
    handleErrors();

  /* Generate key */
  if (EVP_PKEY_keygen(ctx, pkey) <= 0)
    handleErrors();

  EVP_PKEY_CTX_free(ctx);
}

int write_public_rsa_key_to_file(EVP_PKEY *pkey,
                                 const char *public_key_filename) {
  FILE *f;
  f = fopen(public_key_filename, "wb");
  if (f == NULL) {
    return EXIT_FAILURE;
  }
  if (i2d_PUBKEY_fp(f, pkey) < 0) {
    fclose(f);
    return EXIT_FAILURE;
  }
  fclose(f);
  return EXIT_SUCCESS;
}

int write_private_rsa_key_to_file(EVP_PKEY *pkey,
                                  const char *private_key_filename) {
  FILE *f;
  f = fopen(private_key_filename, "wb");
  if (f == NULL) {
    return EXIT_FAILURE;
  }
  if (i2d_PrivateKey_fp(f, pkey) < 0) {
    fclose(f);
    return EXIT_FAILURE;
  }
  fclose(f);
  return EXIT_SUCCESS;
}

int load_public_rsa_key_from_file(EVP_PKEY **pkey,
                                  const char *public_key_filename) {
  FILE *f;
  f = fopen(public_key_filename, "rb");
  if (f == NULL) {
    return EXIT_FAILURE;
  }
  *pkey = d2i_PUBKEY_fp(f, NULL);
  fclose(f);
  if (*pkey == NULL) {
    return EXIT_FAILURE;
  }
  return EXIT_SUCCESS;
}

int load_private_rsa_key_from_file(EVP_PKEY **pkey,
                                   const char *private_key_filename) {
  FILE *f;
  f = fopen(private_key_filename, "rb");
  if (f == NULL) {
    return EXIT_FAILURE;
  }
  *pkey = d2i_PrivateKey_fp(f, NULL);
  fclose(f);
  if (*pkey == NULL) {
    return EXIT_FAILURE;
  }
  return EXIT_SUCCESS;
}

void printPublicKey(EVP_PKEY *pkey) {
  // buffer
  BIO *bio = BIO_new(BIO_s_file());
  // stdout
  BIO_set_fp(bio, stdout, BIO_NOCLOSE);
  // pass buffer, indent = 0 is just for printout does not matter
  EVP_PKEY_print_public(bio, pkey, 0, NULL);
}

void printPrivateKey(EVP_PKEY *pkey) {
  // buffer
  BIO *bio = BIO_new(BIO_s_file());
  // stdout
  BIO_set_fp(bio, stdout, BIO_NOCLOSE);
  // pass buffer, indent = 0 is just for printout does not matter
  EVP_PKEY_print_private(bio, pkey, 0, NULL);
}

unsigned char *getPublicKey(EVP_PKEY *pkey) {
  // buffer
  BIO *bio = BIO_new(BIO_s_mem());
  // pass buffer, indent = 0 is just for printout does not matter
  EVP_PKEY_print_public(bio, pkey, 0, NULL);
  // length buffer
  int bioLen = BIO_pending(bio);
  // char key
  unsigned char *key = (unsigned char *)malloc(bioLen);
  // extract from buffer to char key
  BIO_read(bio, key, bioLen);
  return key;
}

unsigned char *getPrivateKey(EVP_PKEY *pkey) {
  // buffer
  BIO *bio = BIO_new(BIO_s_mem());
  // pass buffer, indent = 0 is just for printout does not matter
  EVP_PKEY_print_private(bio, pkey, 0, NULL);
  // length buffer
  int bioLen = BIO_pending(bio);
  // char key
  unsigned char *key = (unsigned char *)malloc(bioLen);
  // extract from buffer to char key
  BIO_read(bio, key, bioLen);
  return key;
}

void generateRandBytes(int size, unsigned char *output) {
  if (1 != RAND_bytes(output, size)) {
    handleErrors();
  }
}

int encrypt(unsigned char *plaintext, int plaintext_len, unsigned char *key,
            unsigned char *iv, unsigned char *ciphertext) {
  EVP_CIPHER_CTX *ctx = NULL;
  int len;
  int ciphertext_len;

  /* Create and initialize the context */
  if (!(ctx = EVP_CIPHER_CTX_new()))
    handleErrors();

  /*
   * Initialize the encryption operation. IMPORTANT - ensure you use a key
   * and IV size appropriate for your cipher
   * Here we are using 256 bit AES (i.e. a 256 bit key).
   * The IV size for *most* modes is the same as the block size.
   * For AES this is 128 bits
   */
  if (1 != EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, key, iv))
    handleErrors();

  /*
   * Provide the message to be encrypted, and obtain the encrypted output.
   * EVP_EncryptUpdate can be called multiple times if necessary
   */
  if (1 != EVP_EncryptUpdate(ctx, ciphertext, &len, plaintext, plaintext_len))
    handleErrors();
  ciphertext_len = len;

  /*
   * Finalize the encryption. Further ciphertext bytes may be written at this
   * stage (padding).
   */
  if (1 != EVP_EncryptFinal_ex(ctx, ciphertext + len, &len))
    handleErrors();
  ciphertext_len += len;

  /* Clean up */
  EVP_CIPHER_CTX_free(ctx);

  return ciphertext_len;
}

int decrypt(unsigned char *ciphertext, int ciphertext_len, unsigned char *key,
            unsigned char *iv, unsigned char *plaintext) {
  EVP_CIPHER_CTX *ctx = NULL;
  int len;
  int plaintext_len;

  /* Create and initialize the context */
  if (!(ctx = EVP_CIPHER_CTX_new()))
    handleErrors();

  /*
   * Initialize the decryption operation. IMPORTANT - ensure you use a key
   * and IV size appropriate for your cipher
   * Here we are using 256 bit AES (i.e. a 256 bit key).
   * The IV size for *most* modes is the same as the block size.
   * For AES this is 128 bits
   */
  if (1 != EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), NULL, key, iv))
    handleErrors();

  /*
   * Provide the message to be decrypted, and obtain the plaintext output.
   * EVP_DecryptUpdate can be called multiple times if necessary.
   */
  if (1 != EVP_DecryptUpdate(ctx, plaintext, &len, ciphertext, ciphertext_len))
    handleErrors();
  plaintext_len = len;

  /*
   * Finalize the decryption. Further plaintext bytes may be written at
   * this stage.
   */
  if (1 != EVP_DecryptFinal_ex(ctx, plaintext + len, &len))
    handleErrors();
  plaintext_len += len;

  /* Clean up */
  EVP_CIPHER_CTX_free(ctx);

  return plaintext_len;
}

int key_derivation(unsigned char *secret, int size_secret_key,
                   unsigned char *derived, int size_derived_key) {
  // KDF derivation Function
  EVP_KDF *kdf;
  EVP_KDF_CTX *kctx = NULL;
  OSSL_PARAM params[3], *p = params;

  /* Find and allocate a context for the HKDF algorithm */
  if ((kdf = EVP_KDF_fetch(NULL, "hkdf", NULL)) == NULL) {
    handleErrors();
  }
  kctx = EVP_KDF_CTX_new(kdf);
  EVP_KDF_free(kdf); /* The kctx keeps a reference so this is safe */
  if (kctx == NULL) {
    handleErrors();
  }

  /* Build up the parameters for the derivation */
  *p++ = OSSL_PARAM_construct_utf8_string("digest", "sha256", (size_t)7);
  *p++ = OSSL_PARAM_construct_octet_string("key", secret, size_secret_key);
  *p = OSSL_PARAM_construct_end();
  if (EVP_KDF_CTX_set_params(kctx, params) <= 0) {
    handleErrors();
  }

  /* Do the derivation */
  if (EVP_KDF_derive(kctx, derived, size_derived_key, NULL) <= 0) {
    handleErrors();
  }
  EVP_KDF_CTX_free(kctx);

  return EXIT_SUCCESS;
}

void handleErrors(void) {
  /*
  Functions called to handle errors if any during the encryption/decryption
  process It just outputs the error log to stderr
  */
  ERR_print_errors_fp(stderr);
  abort();
}

// Sealing an envelope: we use asymmetric encryption to encrypt the generated
// symmetric key that is used to encrypt the plaintext
int envelope_seal(EVP_PKEY **pub_key, unsigned char *plaintext,
                  int plaintext_len, unsigned char **encrypted_key,
                  int *encrypted_key_len, unsigned char *iv,
                  unsigned char *ciphertext) {
  EVP_CIPHER_CTX *ctx = NULL;
  int ciphertext_len;
  int len;

  /* Create and initialize the context */
  if (!(ctx = EVP_CIPHER_CTX_new()))
    handleErrors();

  /* Initialize the envelope seal operation. This operation generates
   * a key for the provided cipher, and then encrypts that key a number
   * of times (one for each public key provided in the pub_key array). In
   * this example the array size is just one. This operation also
   * generates an IV and places it in iv. */
  if (1 != EVP_SealInit(ctx, EVP_aes_256_gcm(), encrypted_key,
                        encrypted_key_len, iv, pub_key, 1))
    handleErrors();

  /* Provide the message to be encrypted, and obtain the encrypted output.
   * EVP_SealUpdate can be called multiple times if necessary
   */
  if (1 != EVP_SealUpdate(ctx, ciphertext, &len, plaintext, plaintext_len))
    handleErrors();
  ciphertext_len = len;

  /* Finalize the encryption. Further ciphertext bytes may be written at
   * this stage.
   */
  if (1 != EVP_SealFinal(ctx, ciphertext + len, &len))
    handleErrors();
  ciphertext_len += len;

  /* Clean up */
  EVP_CIPHER_CTX_free(ctx);

  return ciphertext_len;
}

int envelope_open(EVP_PKEY *priv_key, unsigned char *ciphertext,
                  int ciphertext_len, unsigned char *encrypted_key,
                  int encrypted_key_len, unsigned char *iv,
                  unsigned char *plaintext) {
  EVP_CIPHER_CTX *ctx = NULL;
  int len;
  int plaintext_len;

  /* Create and initialize the context */
  if (!(ctx = EVP_CIPHER_CTX_new()))
    handleErrors();

  /* Initialize the decryption operation. The asymmetric private key is
   * provided and priv_key, whilst the encrypted session key is held in
   * encrypted_key */
  if (1 != EVP_OpenInit(ctx, EVP_aes_256_gcm(), encrypted_key,
                        encrypted_key_len, iv, priv_key))
    handleErrors();

  /* Provide the message to be decrypted, and obtain the plaintext output.
   * EVP_OpenUpdate can be called multiple times if necessary
   */
  if (1 != EVP_OpenUpdate(ctx, plaintext, &len, ciphertext, ciphertext_len))
    handleErrors();
  plaintext_len = len;

  /* Finalize the decryption. Further plaintext bytes may be written at
   * this stage.
   */
  if (1 != EVP_OpenFinal(ctx, plaintext + len, &len))
    handleErrors();
  plaintext_len += len;

  /* Clean up */
  EVP_CIPHER_CTX_free(ctx);

  return plaintext_len;
}

// Digest message, compute hash value
void digest_message(unsigned char *message, int message_len,
                    unsigned char **digest, unsigned int *digest_len) {
  EVP_MD_CTX *mdctx = NULL;

  if ((mdctx = EVP_MD_CTX_new()) == NULL)
    handleErrors();

  if (1 != EVP_DigestInit_ex(mdctx, EVP_sha256(), NULL))
    handleErrors();

  if (1 != EVP_DigestUpdate(mdctx, message, message_len))
    handleErrors();

  if ((*digest = (unsigned char *)OPENSSL_malloc(EVP_MD_size(EVP_sha256()))) ==
      NULL)
    handleErrors();

  if (1 != EVP_DigestFinal_ex(mdctx, *digest, digest_len))
    handleErrors();

  EVP_MD_CTX_free(mdctx);
}

size_t hmac_sha256(unsigned char *message, int message_len, unsigned char *iv,
                   int iv_len, unsigned long long *timestamp,
                   unsigned char *hash, unsigned char *key, int key_len) {
  EVP_MD_CTX *mdctx = NULL;
  const EVP_MD *md = NULL;
  EVP_PKEY *pkey = NULL;
  size_t hash_len = 0;

  if (!(mdctx = EVP_MD_CTX_create()))
    handleErrors();

  if (!(md = EVP_get_digestbyname("SHA256")))
    handleErrors();

  if (!(pkey = EVP_PKEY_new_mac_key(EVP_PKEY_HMAC, NULL, key, key_len)))
    handleErrors();

  if (EVP_DigestInit_ex(mdctx, md, NULL) != 1)
    handleErrors();

  if (EVP_DigestSignInit(mdctx, NULL, md, NULL, pkey) != 1)
    handleErrors();
  // message (cipherdata)
  if (EVP_DigestSignUpdate(mdctx, message, message_len) != 1)
    handleErrors();
  // timestamp
  if (EVP_DigestSignUpdate(mdctx, iv, iv_len) != 1)
    handleErrors();
  // timestamp
  if (EVP_DigestSignUpdate(mdctx, timestamp, sizeof(unsigned long long)) != 1)
    handleErrors();

  if (EVP_DigestSignFinal(mdctx, hash, &hash_len) != 1)
    handleErrors();

  EVP_MD_CTX_destroy(mdctx);
  return hash_len;
}

int verify_hmac_sha256(unsigned char *message, int message_len,
                       unsigned char *iv, int iv_len,
                       unsigned long long *timestamp, unsigned char *hash,
                       int hash_len, unsigned char *key, int key_len) {
  unsigned char buff[32];

  int size = hmac_sha256(message, message_len, iv, iv_len, timestamp, buff, key,
                         key_len);

  const int len = (hash_len < size ? hash_len : size);
  int result = CRYPTO_memcmp(hash, buff, len);

  OPENSSL_cleanse(buff, sizeof(buff));

  return (result == 0 ? EXIT_SUCCESS : EXIT_FAILURE);
}

size_t sign_rsa(unsigned char *previous_hash, int previous_hash_len,
                unsigned char *current_hash, int current_hash_len,
                unsigned char *sig, EVP_PKEY *pkey) {
  EVP_MD_CTX *mdctx = NULL;
  const EVP_MD *md = NULL;
  size_t sig_len = 0;

  if (!(mdctx = EVP_MD_CTX_create()))
    handleErrors();

  if (!(md = EVP_get_digestbyname("SHA256")))
    handleErrors();

  if (EVP_DigestInit_ex(mdctx, md, NULL) != 1)
    handleErrors();

  if (EVP_DigestSignInit(mdctx, NULL, md, NULL, pkey) != 1)
    handleErrors();

  if (EVP_DigestSignUpdate(mdctx, previous_hash, previous_hash_len) != 1)
    handleErrors();

  if (EVP_DigestSignUpdate(mdctx, current_hash, current_hash_len) != 1)
    handleErrors();

  if (EVP_DigestSignFinal(mdctx, sig, &sig_len) != 1)
    handleErrors();

  EVP_MD_CTX_destroy(mdctx);
  return sig_len;
}

int verify_sign_rsa(unsigned char *previous_hash, int previous_hash_len,
                    unsigned char *current_hash, int current_hash_len,
                    unsigned char *sig, int sig_len, EVP_PKEY *pkey) {
  EVP_MD_CTX *mdctx = NULL;
  const EVP_MD *md = NULL;
  int result = EXIT_FAILURE;

  if (!(mdctx = EVP_MD_CTX_create()))
    handleErrors();

  if (!(md = EVP_get_digestbyname("SHA256")))
    handleErrors();

  if (EVP_DigestInit_ex(mdctx, md, NULL) != 1)
    handleErrors();

  if (EVP_DigestVerifyInit(mdctx, NULL, md, NULL, pkey) != 1)
    handleErrors();

  if (EVP_DigestVerifyUpdate(mdctx, previous_hash, previous_hash_len) != 1)
    handleErrors();

  if (EVP_DigestVerifyUpdate(mdctx, current_hash, current_hash_len) != 1)
    handleErrors();

  /* Clear any errors for the call below */
  ERR_clear_error();

  if (EVP_DigestVerifyFinal(mdctx, sig, sig_len) != 1) {
    result = EXIT_FAILURE;
  } else {
    result = EXIT_SUCCESS;
  }

  EVP_MD_CTX_destroy(mdctx);
  return result;
}
