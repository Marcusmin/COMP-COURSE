# Randomly fills a grid of size 10 x 10 with 0s and 1s,
# in an estimated proportion of 1/2 for each,
# and computes the longest leftmost path that starts
# from the top left corner -- a path consisting of
# horizontally or vertically adjacent 1s --,
# visiting every point on the path once only.
#
# Written by *** and Eric Martin for COMP9021


import sys
from random import seed, randrange

from queue_adt import *


def display_grid():
    for i in range(len(grid)):
        print('   ', ' '.join(str(grid[i][j]) for j in range(len(grid[0]))))

def leftmost_longest_path_from_top_left_corner():
    queue = Queue()
    if grid[0][0] == 0:
        return None
    queue.enqueue([(0,0)])
    longest_path = [(0,0)]
    while not queue.is_empty():
        path = queue.dequeue()
        #print(path)
        if len(longest_path) < len(path):
            longest_path = path
        arrow_point = path[-1]
        ## Make sure point is in grid
        if len(path) < 2:
            if arrow_point[0] < len(grid) and arrow_point[1] < len(grid) and arrow_point[0] >= 0 \
               and arrow_point[1] >= 0:
                if arrow_point[1] + 1 < len(grid):
                    ##E
                    if (arrow_point[0], arrow_point[1] + 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] + 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] + 1)])
                if arrow_point[0] + 1 < len(grid):
                    ##S
                    if (arrow_point[0] + 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] + 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] + 1, arrow_point[1])])
                if arrow_point[1] - 1 >= 0:
                    ##W
                    if (arrow_point[0], arrow_point[1] - 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] - 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] - 1)])
                if arrow_point[0] - 1 >= 0:
                    ##N
                    if (arrow_point[0] - 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] - 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] - 1, arrow_point[1])])
        else:
            pre_arrow_point = path[-2]
            ##Point to East
            if pre_arrow_point[0] == arrow_point[0] and arrow_point[1] - pre_arrow_point[1] > 0:
##                if arrow_point[0] < len(grid) and arrow_point[1] < len(grid) and arrow_point[0] >= 0 \
##               and arrow_point[1] >= 0:
                if arrow_point[0] - 1 >= 0:
                    ##N
                    if (arrow_point[0] - 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] - 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] - 1, arrow_point[1])])
                if arrow_point[1] + 1 < len(grid):
                    ##E
                    if (arrow_point[0], arrow_point[1] + 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] + 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] + 1)])
                if arrow_point[0] + 1 < len(grid):
                    ##S
                    if (arrow_point[0] + 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] + 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] + 1, arrow_point[1])])
                if arrow_point[1] - 1 >= 0:
                    ##W
                    if (arrow_point[0], arrow_point[1] - 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] - 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] - 1)])
                
            ##Point to West
            if pre_arrow_point[0] == arrow_point[0] and arrow_point[1] - pre_arrow_point[1] < 0:
##                if arrow_point[0] < len(grid) and arrow_point[1] < len(grid) and arrow_point[0] >= 0 \
##               and arrow_point[1] >= 0:
                if arrow_point[0] + 1 < len(grid):
                    ##S
                    if (arrow_point[0] + 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] + 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] + 1, arrow_point[1])])
                if arrow_point[1] - 1 >= 0:
                    ##W
                    if (arrow_point[0], arrow_point[1] - 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] - 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] - 1)])
                if arrow_point[0] - 1 >= 0:
                    ##N
                    if (arrow_point[0] - 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] - 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] - 1, arrow_point[1])])
                if arrow_point[1] + 1 < len(grid):
                    ##E
                    if (arrow_point[0], arrow_point[1] + 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] + 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] + 1)])
                
            ##Point to South
            if pre_arrow_point[1] == arrow_point[1] and arrow_point[0] - pre_arrow_point[0] > 0:
##                if arrow_point[0] < len(grid) and arrow_point[1] < len(grid) and arrow_point[0] >= 0 \
##               and arrow_point[1] >= 0:
                if arrow_point[1] + 1 < len(grid):
                    ##E
                    if (arrow_point[0], arrow_point[1] + 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] + 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] + 1)])
                if arrow_point[0] + 1 < len(grid):
                    ##S
                    if (arrow_point[0] + 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] + 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] + 1, arrow_point[1])])
                if arrow_point[1] - 1 >= 0:
                    ##W
                    if (arrow_point[0], arrow_point[1] - 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] - 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] - 1)])
                if arrow_point[0] - 1 >= 0:
                    ##N
                    if (arrow_point[0] - 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] - 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] - 1, arrow_point[1])])
            ##Point to North
            if pre_arrow_point[1] == arrow_point[1] and arrow_point[0] - pre_arrow_point[0] < 0:
##                if arrow_point[0] < len(grid) and arrow_point[1] < len(grid) and arrow_point[0] >= 0 \
##               and arrow_point[1] >= 0:
                if arrow_point[1] - 1 >= 0:
                    ##W
                    if (arrow_point[0], arrow_point[1] - 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] - 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] - 1)])
                if arrow_point[0] - 1 >= 0:
                    ##N
                    if (arrow_point[0] - 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] - 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] - 1, arrow_point[1])])
                if arrow_point[1] + 1 < len(grid):
                    ##E
                    if (arrow_point[0], arrow_point[1] + 1) not in path \
                       and grid[arrow_point[0]][arrow_point[1] + 1] == 1:
                        queue.enqueue(path + [(arrow_point[0], arrow_point[1] + 1)])
                if arrow_point[0] + 1 < len(grid):
                    ##S
                    if (arrow_point[0] + 1, arrow_point[1]) not in path \
                       and grid[arrow_point[0] + 1][arrow_point[1]] == 1:
                        queue.enqueue(path + [(arrow_point[0] + 1, arrow_point[1])])
                
##    path = queue.dequeue()
##    if len(longest_path) < len(path):
##        longest_path = path
    return longest_path
        
        
    
        
    # Replace pass above with your code


provided_input = input('Enter one integer: ')
try:
    for_seed = int(provided_input)
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
seed(for_seed)
grid = [[randrange(2) for _ in range(10)] for _ in range(10)]
print('Here is the grid that has been generated:')
display_grid()
path = leftmost_longest_path_from_top_left_corner()
if not path:
    print('There is no path from the top left corner.')
else:
    print(f'The leftmost longest path from the top left corner is: {path}')
           

