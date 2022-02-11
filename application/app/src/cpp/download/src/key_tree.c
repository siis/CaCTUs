/* Include Files */
#include "key_tree.h"

/* Defines */

/* Local Data */

/* Functions */

key_tree *create_key_tree(int depth_tree, int delta_time, key_leaf *seed_leaf) {
  key_tree *tree = (key_tree *)malloc(sizeof(key_tree));
  tree->depth_tree = depth_tree;
  tree->delta_time = delta_time;
  tree->seed_leaf = seed_leaf;
  return tree;
}

key_leaf *create_key_leaf(int depth_leaf, int time_inf, void *key) {
  key_leaf *leaf = (key_leaf *)malloc(sizeof(key_leaf));
  leaf->depth_leaf = depth_leaf;
  leaf->time_inf = time_inf;
  leaf->key = key;
  leaf->left_leaf = NULL;
  leaf->right_leaf = NULL;
  return leaf;
}

void set_left_leaf(key_leaf *leaf, key_leaf *left_leaf) {
  leaf->left_leaf = left_leaf;
}

void set_right_leaf(key_leaf *leaf, key_leaf *right_leaf) {
  leaf->right_leaf = right_leaf;
}

void set_key_leaf(key_leaf *leaf, unsigned char *key) { leaf->key = key; }

key_leaf *get_left_leaf(key_leaf *leaf) { return leaf->left_leaf; }

key_leaf *get_right_leaf(key_leaf *leaf) { return leaf->right_leaf; }

double compute_time_sup(double time_inf, int delta_time, int depth_tree,
                        int depth_leaf) {
  return time_inf + delta_time * pow(2, depth_tree - 1 - depth_leaf);
}

bool it_is_a_top_leaf(key_leaf *leaf, key_tree *tree) {
  return (leaf->depth_leaf == tree->depth_tree);
}

bool key_is_null(key_leaf *leaf) { return (leaf->key == NULL); }

bool upper_keys_not_created(key_leaf *leaf) {
  return (leaf->left_leaf == NULL && leaf->right_leaf == NULL);
}

void delete_leaf_key(key_leaf *leaf) {
  free(leaf->key);
  leaf->key = NULL;
}

void generate_upper_leaves(key_leaf *leaf, key_tree *tree,
                           bool delete_parent_key) {
  if (!it_is_a_top_leaf(leaf, tree) && upper_keys_not_created(leaf)) {
    // if we are not at the top of the tree and the upper_keys have
    // not already been created
    unsigned char *key_l = (unsigned char *)malloc(32);
    unsigned char *key_r = (unsigned char *)malloc(32);
    unsigned char plus_one_key[32];
    for (size_t i = 0; i < 32; ++i) {
      plus_one_key[i] = (unsigned char)(((int)leaf->key[i]) + 1);
    }
    // derive keys for upper keys
    key_derivation(leaf->key, 32, key_l, 32);
    key_derivation(plus_one_key, 32, key_r, 32);
    double time_sup = compute_time_sup(leaf->time_inf, tree->delta_time,
                                       tree->depth_tree, leaf->depth_leaf);

    key_leaf *left_leaf =
        create_key_leaf(leaf->depth_leaf + 1, leaf->time_inf, key_l);
    key_leaf *right_leaf =
        create_key_leaf(leaf->depth_leaf + 1, time_sup / 2, key_r);
    leaf->left_leaf = left_leaf;
    leaf->right_leaf = right_leaf;

    if (delete_parent_key) {
      delete_leaf_key(leaf);
    }
  }
}

bool find_key_for_timestamp(double timestamp, key_tree *tree,
                            bool delete_parent_key, unsigned char **key) {
  key_leaf **current_leaf;
  current_leaf = &(tree->seed_leaf);
  double time_inf = (*current_leaf)->time_inf;
  double time_sup =
      compute_time_sup(time_inf, tree->delta_time, tree->depth_tree,
                       (*current_leaf)->depth_leaf);

  if (!((timestamp < time_sup) && (timestamp >= time_inf))) {
    // if timestamp not reachable in tree abort
    *key = NULL;
    return false;
  }
  for (int i = 0; i < tree->depth_tree - 1; i++) {
    time_inf = (*current_leaf)->time_inf;
    time_sup = compute_time_sup(time_inf, tree->delta_time, tree->depth_tree,
                                (*current_leaf)->depth_leaf);

    // call to generate in case upper leaves do not exist yet
    generate_upper_leaves((*current_leaf), tree, delete_parent_key);
    if (timestamp < (time_inf + time_sup) / 2) {
      current_leaf = &(*current_leaf)->left_leaf;
    } else {
      current_leaf = &(*current_leaf)->right_leaf;
    }
  }
  // maybe add a check on the depth of the key to check that we are at the top
  // of the tree
  *key = (*current_leaf)->key;
  return true;
}

bool find_leaf_delegation_between_two_timestamps(double timestamp1,
                                                 double timestamp2,
                                                 key_tree *tree,
                                                 bool delete_parent_key,
                                                 key_leaf **leaf_to_delegate) {
  // approximation with floor and ceil, maybe delegation more precise required
  // in the future
  key_leaf **current_leaf;
  current_leaf = &(tree->seed_leaf);
  double time_inf = (*current_leaf)->time_inf;
  double time_sup =
      compute_time_sup(time_inf, tree->delta_time, tree->depth_tree,
                       (*current_leaf)->depth_leaf);

  timestamp1 = (double)(floor((double)timestamp1 / (2 * tree->delta_time)) *
                        (2 * tree->delta_time));
  timestamp2 = (double)(ceil((double)timestamp2 / (2 * tree->delta_time)) *
                        (2 * tree->delta_time));

  if (!((timestamp1 < time_sup) && (timestamp1 >= time_inf)) ||
      !((timestamp2 <= time_sup) && (timestamp2 >= time_inf))) {
    // if timestamp not reachable in tree abort
    *leaf_to_delegate = NULL;
    return false;
  }
  while ((time_inf != timestamp1) || (time_sup != timestamp2)) {
    generate_upper_leaves((*current_leaf), tree, delete_parent_key);
    if (timestamp1 < (time_inf + time_sup) / 2) {
      current_leaf = &(*current_leaf)->left_leaf;
    } else {
      current_leaf = &(*current_leaf)->right_leaf;
    }
    time_inf = (*current_leaf)->time_inf;
    time_sup = compute_time_sup(time_inf, tree->delta_time, tree->depth_tree,
                                (*current_leaf)->depth_leaf);
  }
  *leaf_to_delegate = *current_leaf;
  return true;
}