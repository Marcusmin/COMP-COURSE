# Randomly generates a binary search tree whose number of nodes
# is determined by user input, with labels ranging between 0 and 999,999,
# displays it, and outputs the maximum difference between consecutive leaves.
#
# Written by *** and Eric Martin for COMP9021

import sys
from random import seed, randrange
from binary_tree_adt import *

# Possibly define some functions
def find_leaves(tree,leaves = []):
    if len(tree.post_order_traversal()) == 1:
        leaves.extend(tree.post_order_traversal())
    if len(tree.post_order_traversal()) > 1:
        find_leaves(tree.left_node)
        find_leaves(tree.right_node)
    return leaves
        

def max_diff_in_consecutive_leaves(tree):
    leaves = find_leaves(tree)
    max_diff = 0
    if len(leaves) <= 1:
        return max_diff
    else:
        for i in range(len(leaves) - 1):
            if abs(leaves[i + 1] - leaves[i]) > max_diff:
                max_diff = abs(leaves[i + 1] - leaves[i])
        return max_diff
    # Replace pass above with your code


provided_input = input('Enter two integers, the second one being positive: ')
try:
    arg_for_seed, nb_of_nodes = provided_input.split()
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
try:
    arg_for_seed, nb_of_nodes = int(arg_for_seed), int(nb_of_nodes)
    if nb_of_nodes < 0:
        raise ValueError
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
seed(arg_for_seed)
tree = BinaryTree()
for _ in range(nb_of_nodes):
    datum = randrange(1000000)
    tree.insert_in_bst(datum)
print('Here is the tree that has been generated:')
tree.print_binary_tree()
print('The maximum difference between consecutive leaves is: ', end = '')
print(max_diff_in_consecutive_leaves(tree))
           

