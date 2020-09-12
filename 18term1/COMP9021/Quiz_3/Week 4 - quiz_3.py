# Randomly generates a grid with 0s and 1s, whose dimension is controlled by user input,
# as well as the density of 1s in the grid, and finds out, for a given direction being
# one of N, E, S or W (for North, East, South or West) and for a given size greater than 1,
# the number of triangles pointing in that direction, and of that size.
#
# Triangles pointing North:
# - of size 2:
#   1
# 1 1 1
# - of size 3:
#     1
#   1 1 1
# 1 1 1 1 1
#
# Triangles pointing East:
# - of size 2:
# 1
# 1 1
# 1
# - of size 3:
# 1
# 1 1
# 1 1 1 
# 1 1
# 1
#
# Triangles pointing South:
# - of size 2:
# 1 1 1
#   1
# - of size 3:
# 1 1 1 1 1
#   1 1 1
#     1
#
# Triangles pointing West:
# - of size 2:
#   1
# 1 1
#   1
# - of size 3:
#     1
#   1 1
# 1 1 1 
#   1 1
#     1
#
# The output lists, for every direction and for every size, the number of triangles
# pointing in that direction and of that size, provided there is at least one such triangle.
# For a given direction, the possble sizes are listed from largest to smallest.
#
# We do not count triangles that are truncations of larger triangles, that is, obtained
# from the latter by ignoring at least one layer, starting from the base.
#
# Written by *** and Eric Martin for COMP9021


from random import seed, randint
import sys
from collections import defaultdict


def display_grid():
    for i in range(len(grid)):
        print('   ', ' '.join(str(int(grid[i][j] != 0)) for j in range(len(grid))))

def get_triangle(grid):
    count_triangle = defaultdict(int)
    for row in range(0, len(grid)):
        for col in range(1, len(grid) - 1):
            if grid[row][col] == 1:
                size_of_triangle = (len(grid)) + 1 //2
                while size_of_triangle >= 2:
                    if row + size_of_triangle <= len(grid) and col + size_of_triangle <= len(
                            grid) and col - size_of_triangle + 1 >= 0:
                        standard_sum = 0
                        count_one = 0
                        for row_element in range(1, size_of_triangle):
                            for col_element in range(col - row_element,
                                                     col + row_element+1):
                                standard_sum += 1
                                count_one += grid[row + row_element][col_element]
                        if standard_sum == count_one:
                            count_triangle[size_of_triangle] += 1
                            break
                    size_of_triangle -= 1
    return count_triangle




def triangles_in_grid():
    grid_triangle = defaultdict(str)
    new_grid_N = grid.copy()
    for i in range(len(grid)):
        for j in range(len(grid)):
            new_grid_N[i][j] = int(grid[i][j] != 0)
    count_triangle_in_N = get_triangle(new_grid_N)
    if count_triangle_in_N:
        grid_triangle['N'] = []
        count_triangle_in_N = sorted(count_triangle_in_N.items(), key=lambda item: item[0], reverse=True)
        for key, value in count_triangle_in_N:
            grid_triangle['N'].append((key, value))

    new_grid_W = [[0] * len(new_grid_N) for i in range(len(new_grid_N))]
    for col in range(len(new_grid_N)):
        for row in range(len(new_grid_N)):
            new_grid_W[col][len(new_grid_N) - 1 - row] = new_grid_N[row][col]
    count_triangle_in_W = get_triangle(new_grid_W)
    if count_triangle_in_W:
        grid_triangle['W'] = []
        count_triangle_in_W = sorted(count_triangle_in_W.items(),key = lambda item:item[0],reverse=True)
        for key,value in count_triangle_in_W:
            grid_triangle['W'].append((key,value))

    new_grid_E = [[0] * len(new_grid_N) for i in range(len(new_grid_N))]
    for row in range(len(new_grid_N)):
        for col in range(len(new_grid_N)):
            new_grid_E[len(new_grid_N) - 1 - col][row] = new_grid_N[row][col]
    count_triangle_in_E = get_triangle(new_grid_E)
    if count_triangle_in_E:
        grid_triangle['E'] = []
        count_triangle_in_E = sorted(count_triangle_in_E.items(), key=lambda item: item[0], reverse=True)
        for key, value in count_triangle_in_E:
            grid_triangle['E'].append((key, value))

    new_grid_S = [[0] * len(new_grid_N) for i in range(len(new_grid_N))]
    for row in range(len(new_grid_N)):
        for col in range(len(new_grid_N)):
            new_grid_S[len(new_grid_N) - 1 - row][len(new_grid_N) - 1 - col] = new_grid_N[row][col]
    count_triangle_in_S = get_triangle(new_grid_S)

    if count_triangle_in_S:
        grid_triangle['S'] = []
        count_triangle_in_S = sorted(count_triangle_in_S.items(), key=lambda item: item[0], reverse=True)
        for key,value in count_triangle_in_S:
            grid_triangle['S'].append((key, value))


    return grid_triangle
    # Replace return {} above with your code

# Possibly define other functions

try:
    arg_for_seed, density, dim = input('Enter three nonnegative integers: ').split()
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
try:
    arg_for_seed, density, dim = int(arg_for_seed), int(density), int(dim)
    if arg_for_seed < 0 or density < 0 or dim < 0:
        raise ValueError
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()
seed(arg_for_seed)
grid = [[randint(0, density) for _ in range(dim)] for _ in range(dim)]
print('Here is the grid that has been generated:')
display_grid()
# A dictionary whose keys are amongst 'N', 'E', 'S' and 'W',
# and whose values are pairs of the form (size, number_of_triangles_of_that_size),
# ordered from largest to smallest size.
triangles = triangles_in_grid()
for direction in sorted(triangles, key = lambda x: 'NESW'.index(x)):
    print(f'\nFor triangles pointing {direction}, we have:')
    for size, nb_of_triangles in triangles[direction]:
        triangle_or_triangles = 'triangle' if nb_of_triangles == 1 else 'triangles'
        print(f'     {nb_of_triangles} {triangle_or_triangles} of size {size}')
