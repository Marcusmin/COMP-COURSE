# Randomly fills a grid of size 10 x 10 with 0s and 1s and computes:
# - the size of the largest homogenous region starting from the top left corner,
#   so the largest region consisting of connected cells all filled with 1s or
#   all filled with 0s, depending on the value stored in the top left corner;
# - the size of the largest area with a checkers pattern.
#
# Written by *** and Eric Martin for COMP9021

import sys
from random import seed, randint


dim = 10
grid = [[None] * dim for _ in range(dim)]
trace_map = [[None] * dim for _ in range(dim)]


def display_grid():
    for i in range(dim):
        print('   ', ' '.join(str(int(grid[i][j] != 0)) for j in range(dim)))
        
# Possibly define other functions
def find_homo_area(grid,trace_map,row = 0,col = 0):
    global count
    trace_map[row][col] = 'a'
    count+=1
    if row+1<len(grid) and grid[row][col] == grid[row+1][col]:
        if trace_map[row+1][col] == grid[row+1][col]:
            find_homo_area(grid,trace_map,row+1,col)
    if col+1<len(grid) and grid[row][col] == grid[row][col+1]:
        if trace_map[row][col+1] == grid[row][col+1]:
            find_homo_area(grid,trace_map,row,col+1)
    if col-1>=0 and grid[row][col] == grid[row][col-1]:
        if trace_map[row][col-1] == grid[row][col-1]:
            find_homo_area(grid,trace_map,row,col-1)
    if row-1>=0 and grid[row][col] == grid[row-1][col]:
        if trace_map[row-1][col] == grid[row-1][col]:
            find_homo_area(grid,trace_map,row-1,col)
            
def reset_map(grid,trace_map):
    for i in range(len(grid)):
        for j in range(len(grid)):
            trace_map[i][j] = grid[i][j]

    
def find_check_structure(grid,trace_map,row,col):
    global checker_count
    checker_count += 1
    trace_map[row][col] = 'a'
    if row+1<len(grid) and not grid[row][col] == grid[row+1][col]:
        if not trace_map[row+1][col] == 'a':
            find_check_structure(grid,trace_map,row+1,col)
    if col+1<len(grid) and not grid[row][col] == grid[row][col+1]:
        if not trace_map[row][col+1] == 'a':
            find_check_structure(grid,trace_map,row,col+1)
    if col-1>=0 and not grid[row][col] == grid[row][col-1]:
        if not trace_map[row][col-1] == 'a':
            find_check_structure(grid,trace_map,row,col-1)
    if row-1>=0 and not grid[row][col] == grid[row-1][col]:
        if not trace_map[row-1][col] == 'a':
            find_check_structure(grid,trace_map,row-1,col)
    return checker_count


try:
    arg_for_seed, density = input('Enter two nonnegative integers: ').split()
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
try:
    arg_for_seed, density = int(arg_for_seed), int(density)
    if arg_for_seed < 0 or density < 0:
        raise ValueError
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
seed(arg_for_seed)
# We fill the grid with randomly generated 0s and 1s,
# with for every cell, a probability of 1/(density + 1) to generate a 0.
for i in range(dim):
    for j in range(dim):
        grid[i][j] = int(randint(0, density) != 0)
print('Here is the grid that has been generated:')
display_grid()

size_of_largest_homogenous_region_from_top_left_corner  = 0
# Replace this comment with your code
for i in range(len(grid)):
    for j in range(len(grid)):
        trace_map[i][j] = grid[i][j]

count = 0
checker_structure = []


find_homo_area(grid,trace_map)
size_of_largest_homogenous_region_from_top_left_corner = count

print('The size_of the largest homogenous region from the top left corner is '
      f'{size_of_largest_homogenous_region_from_top_left_corner}.'
     )

max_size_of_region_with_checkers_structure = 0
# Replace this comment with your code
for i in range(len(grid)):
    for j in range(len(grid)):
        trace_map[i][j] = grid[i][j]

checker_structure = []

for i in range(len(grid)):
    for j in range(len(grid)):
        if grid[i][j] == 0:
            checker_count = 0
            reset_map(grid,trace_map)
            checker_structure.append(find_check_structure(grid,trace_map,i,j))

max_size_of_region_with_checkers_structure = sorted(checker_structure,reverse = True)[0]

print('The size of the largest area with a checkers structure is '
      f'{max_size_of_region_with_checkers_structure}.'
     )




            


