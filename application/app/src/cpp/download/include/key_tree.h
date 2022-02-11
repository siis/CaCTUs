#ifndef KEY_TREE_H
#define KEY_TREE_H

/* Include files */
#include "encryption.h"
#include <math.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
/* Structs */

struct key_tree {
  int depth_tree; // >=1
  int delta_time;
  struct key_leaf *seed_leaf;
};

struct key_leaf {
  int depth_leaf; //>=0
  double time_inf;
  unsigned char *key;
  struct key_leaf *left_leaf;
  struct key_leaf *right_leaf;
};

typedef struct key_leaf key_leaf;
typedef struct key_tree key_tree;

/* Functions */

key_tree *create_key_tree(int depth_tree, int delta_time, key_leaf *seed_leaf);
key_leaf *create_key_leaf(int depth_leaf, int time_inf, void *key);

void set_left_leaf(key_leaf *leaf, key_leaf *left_leaf);
void set_right_leaf(key_leaf *leaf, key_leaf *left_leaf);
void set_key_leaf(key_leaf *leaf, unsigned char *key);

key_leaf *get_left_leaf(key_leaf *leaf);
key_leaf *get_right_leaf(key_leaf *leaf);
double compute_time_sup(double time_inf, int delta_time, int depth_tree,
                        int depth_leaf);

bool it_is_a_top_leaf(key_leaf *leaf, key_tree *tree);
bool key_is_null(key_leaf *leaf);
bool upper_keys_not_created(key_leaf *leaf);

void delete_leaf_key(key_leaf *leaf);

void generate_upper_leaves(key_leaf *leaf, key_tree *tree,
                           bool delete_parent_key);

bool find_key_for_timestamp(double timestamp, key_tree *tree,
                            bool delete_parent_key, unsigned char **key);

bool find_leaf_delegation_between_two_timestamps(double timestamp1,
                                                 double timestamp2,
                                                 key_tree *tree,
                                                 bool delete_parent_key,
                                                 key_leaf **leaf_to_delegate);

#endif