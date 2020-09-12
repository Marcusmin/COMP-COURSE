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
                size_of_triangle = len(grid)
                while size_of_triangle >= 2:
                    if row + size_of_triangle <= len(grid) and col + size_of_triangle <= len(
                            grid) and col - size_of_triangle + 1 >= 0:
                        standard_sum = 0
                        count_one = 0
                        for row_element in range(0, size_of_triangle):
                            for col_element in range(col - size_of_triangle + 1 + row_element,
                                                     col + size_of_triangle - row_element):
                                standard_sum += 1
                                count_one += grid[row + size_of_triangle - 1 - row_element][col_element]
                        if standard_sum == count_one:
                            count_triangle[size_of_triangle] += 1
                            break
                    size_of_triangle -= 1
    return count_triangle




def triangles_in_grid():
    grid_triangle = {'N':[],'E':[],'S':[],'W':[]}
    new_grid_N = grid.copy()
    for i in range(len(grid)):
        for j in range(len(grid)):
            new_grid_N[i][j] = int(grid[i][j] != 0)
    count_triangle_in_N = get_triangle(new_grid_N)
    for key in count_triangle_in_N:
        grid_triangle['N'].append((key,count_triangle_in_N[key]))
    new_grid_W = [[0] * len(new_grid_N) for i in range(len(new_grid_N))]
    for col in range(len(new_grid_N)):
        for row in range(len(new_grid_N)):
            new_grid_W[col][len(new_grid_N) - 1 - row] = new_grid_N[row][col]
    count_triangle_in_W = get_triangle(new_grid_W)
    for key in count_triangle_in_W:
        grid_triangle['W'].append((key,count_triangle_in_W[key]))
    new_grid_S = [[0] * len(new_grid_N) for i in range(len(new_grid_N))]
    for row in range(len(new_grid_N)):
        for col in range(len(new_grid_N)):
            new_grid_S[len(new_grid_N) - 1 -row][len(new_grid_N) - 1 -col] = new_grid_N[row][col]
    count_triangle_in_S = get_triangle(new_grid_S)
    for key in count_triangle_in_S:
        grid_triangle['S'].append((key,count_triangle_in_S[key]))
    print(grid_triangle)






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
print(triangles)
#for direction in sorted(triangles, key = lambda x: 'NESW'.index(x)):
#    print(f'\nFor triangles pointing {direction}, we have:')
 #   for size, nb_of_triangles in triangles[direction]:
 #       triangle_or_triangles = 'triangle' if nb_of_triangles == 1 else 'triangles'
   #     print(f'     {nb_of_triangles} {triangle_or_triangles} of size {size}')