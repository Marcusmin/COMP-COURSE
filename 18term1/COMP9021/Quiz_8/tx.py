from quiz_8 import leftmost_longest_path_from_top_left_corner
from random import seed, randrange

def test_find_path_1(for_seed):
    '''
    >>> test_find_path_1(1)

    >>> test_find_path_1(7)
    [(0, 0)]
    >>> test_find_path_1(0)
    [(0, 0), (0, 1), (1, 1)]
    >>> test_find_path_1(9)
    [(0, 0), (0, 1), (0, 2)]
    >>> test_find_path_1(16)
    [(0, 0), (0, 1), (0, 2), (0, 3), (0, 4), (1, 4), (1, 5), (2, 5), (3, 5), (3, 4), (4, 4)]
    >>> test_find_path_1(17)
    [(0, 0), (0, 1), (1, 1), (1, 2), (0, 2), (0, 3), (1, 3), (2, 3), (2, 4), (3, 4), (4, 4), (5, 4), (6, 4), (6, 5), (7, 5), (8, 5), (8, 6), (8, 7), (7, 7), (7, 6), (6, 6), (5, 6), (4, 6), (3, 6), (2, 6)]
    '''
    seed(for_seed)
    grid = [[randrange(2) for _ in range(10)] for _ in range(10)]
    return leftmost_longest_path_from_top_left_corner(grid)

def test_find_path_2(for_seed):
    '''
    >>> test_find_path_2(23)
    [(0, 0), (1, 0), (2, 0), (2, 1), (3, 1), (3, 0), (4, 0)]
    '''
    seed(for_seed)
    grid = [[randrange(2) for _ in range(10)] for _ in range(10)]
    return leftmost_longest_path_from_top_left_corner(grid)

if __name__ == '__main__':
    import doctest
    doctest.testmod()
