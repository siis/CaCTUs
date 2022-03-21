#ifndef ENCRYPTION_H
#define ENCRYPTION_H

/* Include files */
#include <openssl/conf.h>
#include <openssl/ec.h>
#include <openssl/err.h>
#include <openssl/evp.h>
#include <openssl/kdf.h>
#include <openssl/params.h>
#include <openssl/rand.h>
#include <openssl/x509.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Structs */

/* Functions */

/**
 * Create a pair of RSA keys
 */
void generate_rsa_key_pair(EVP_PKEY **pkey);

/**
 *write public key in pkey to file.
 */
int write_public_rsa_key_to_file(EVP_PKEY *pkey,
                                 const char *public_key_filename);

/**
 *write private key in pkey to file
 */
int write_private_rsa_key_to_file(EVP_PKEY *pkey,
                                  const char *private_key_filename);

/**
 *load public key in pkey from file
 */
int load_public_rsa_key_from_file(EVP_PKEY **pkey,
                                  const char *public_key_filename);

/**
 *load private key in pkey from file
 */
int load_private_rsa_key_from_file(EVP_PKEY **pkey,
                                   const char *private_key_filename);

/**
 *prints a textual representation of the public key in pkey.
 */
void printPublicKey(EVP_PKEY *pkey);

/**
 *prints a textual representation of the private key in pkey.
 */
void printPrivateKey(EVP_PKEY *pkey);

/**
 *returns a textual representation of the public key in pkey.
 */
unsigned char *getPublicKey(EVP_PKEY *pkey);

/**
 *returns a textual representation of the private key in pkey.
 */
unsigned char *getPrivateKey(EVP_PKEY *pkey);

/**
 * Generate size RANDOM Bytes and store them into output
 */
void generateRandBytes(int size, unsigned char *output);

/**
 * AES 256 CBC encryption
 * Input: plaintext to encrypt, size text, key, iv
 * Output: encrypted text in ciphertext (via pointer param), size ciphertext
 */
int cbc_encrypt(unsigned char *plaintext, int plaintext_len, unsigned char *key,
                unsigned char *iv, unsigned char *ciphertext);

/**
 * AES 256 CBC decryption
 * Input: ciphertext to decrypt, size text, key, iv
 * Output: decrypted text in plaintext (via pointer param), size plaintext
 */
int cbc_decrypt(unsigned char *ciphertext, int ciphertext_len,
                unsigned char *key, unsigned char *iv,
                unsigned char *plaintext);

/**
 * AES 256 GCM encryption
 * Input: plaintext to encrypt, size text, key, iv
 * Output: encrypted text in ciphertext (via pointer param), size ciphertext
 */
int gcm_encrypt(unsigned char *plaintext, int plaintext_len, unsigned char *aad,
                int aad_len, unsigned char *key, unsigned char *iv, int iv_len,
                unsigned char *ciphertext, unsigned char *tag);

/**
 * AES 256 GCM decryption
 * Input: ciphertext to decrypt, size text, key, iv
 * Output: decrypted text in plaintext (via pointer param), size plaintext
 */
int gcm_decrypt(unsigned char *ciphertext, int ciphertext_len,
                unsigned char *aad, int aad_len, unsigned char *tag,
                unsigned char *key, unsigned char *iv, int iv_len,
                unsigned char *plaintext);

/**
 * HKDF Key Derivation
 * Input: some secret from which we derive a key, size of the key to derive
 * Output: key derived of the corresponding size
 */
int key_derivation(unsigned char *secret, int size_secret_key,
                   unsigned char *derived, int size_derived_key);

/**
 * To handle errors returned by OpenSSL, abort the program
 */
void handleErrors(void);

/*  https://wiki.openssl.org/index.php/EVP_Asymmetric_Encryption_and_Decryption_of_an_Envelope
 */
int envelope_seal(EVP_PKEY **pub_key, unsigned char *plaintext,
                  int plaintext_len, unsigned char **encrypted_key,
                  int *encrypted_key_len, unsigned char *iv,
                  unsigned char *ciphertext);

/*  https://wiki.openssl.org/index.php/EVP_Asymmetric_Encryption_and_Decryption_of_an_Envelope
 */
int envelope_open(EVP_PKEY *priv_key, unsigned char *ciphertext,
                  int ciphertext_len, unsigned char *encrypted_key,
                  int encrypted_key_len, unsigned char *iv,
                  unsigned char *plaintext);

/* https://wiki.openssl.org/index.php/EVP_Message_Digests */
void digest_message(unsigned char *message, int message_len,
                    unsigned char **digest, unsigned int *digest_len);

/* Compute HMAC sha256 of message and return size of hash (normally 64 for
 * sha256) https://wiki.openssl.org/index.php/EVP_Signing_and_Verifying
 * */
size_t hmac_sha256(unsigned char *message, int message_len, unsigned char *iv,
                   int iv_len, unsigned long long *timestamp,
                   unsigned char *hash, unsigned char *key, int key_len);

/* Verify that the hash provided is the HMAC of the message, return 0 if so.
 * CRYPTO_memcmp is used to prevent attacks based on the timing of the
 * comparison https://wiki.openssl.org/index.php/EVP_Signing_and_Verifying
 */
int verify_hmac_sha256(unsigned char *message, int message_len,
                       unsigned char *iv, int iv_len,
                       unsigned long long *timestamp, unsigned char *hash,
                       int hash_len, unsigned char *key, int key_len);
/*
 * https://wiki.openssl.org/index.php/EVP_Signing_and_Verifying
 */
size_t sign_rsa(unsigned char *previous_hash, int previous_hash_len,
                unsigned char *current_hash, int current_hash_len,
                unsigned char *sig, EVP_PKEY *pkey);

/*
 * https://wiki.openssl.org/index.php/EVP_Signing_and_Verifying
 */
int verify_sign_rsa(unsigned char *previous_hash, int previous_hash_len,
                    unsigned char *current_hash, int current_hash_len,
                    unsigned char *sig, int sig_len, EVP_PKEY *pkey);
#endif