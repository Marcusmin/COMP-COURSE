from random import seed, randrange
import sys


dim = 10
tot_chess = 0


def display_grid():
    for i in range(dim):
        print('    ', end = '')
        for j in range(dim):
            print(' 1', end = '') if grid[i][j] else print(' 0', end = '')
        print()
    print()


def explore_board():
    tot_chess = 0
    for i in range(dim):
        for j in range(dim):
            if grid[i][j]:
                grid[i][j] = False
                knight_path(i, j)
                tot_chess += 1
    return tot_chess

def knight_path(x, y):
    moves = knight_possible_moves(x, y)
              
    for i in moves:
        row = i[0]
        col = i[1]
        if grid[row][col]:
            grid[row][col] = False
            #display_grid()
            knight_path(row, col)

# Possibly insert extra code here
def knight_possible_moves(x, y):
    a = 1
    b = 2
    l = []
    for i in range(8):
        a, b = b, a
        a *= -1
        t_0 = a
        t_1 = b
        if i >= 4:
            t_0, t_1 = t_1, t_0
        t_x = x + t_0
        t_y = y + t_1
        #check if coordinations are within boundarys
        if t_x >= 0 and t_x < dim and t_y >= 0 and t_y < dim:
            l.append([t_x, t_y])
        
    return l
    

try:
    for_seed, n = [int(i) for i in
                           input('Enter two integers: ').split()]
    if not n:
        raise ValueError
except ValueError:
    print('Incorrect input, giving up.')
    sys.exit()

seed(for_seed)
grid = [[None] * dim for _ in range(dim)]
if n > 0:
    for i in range(dim):
        for j in range(dim):
            grid[i][j] = randrange(n) > 0
else:
    for i in range(dim):
        for j in range(dim):
            grid[i][j] = randrange(-n) == 0
print('Here is the grid that has been generated:')
display_grid()
nb_of_knights = explore_board()
if not nb_of_knights:
    print('No chess knight has explored this board.')
else:
    print('At least {} chess'.format(nb_of_knights), end = ' ')
    print('knight has', end = ' ') if nb_of_knights == 1 else print('knights have', end = ' ')
    print('explored this board.')