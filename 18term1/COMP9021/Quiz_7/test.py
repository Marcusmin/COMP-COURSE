import sys
from random import seed, randrange

from linked_list_adt import *
from extended_linked_list import ExtendedLinkedList


def collect_references(L, length):
    node = L.head
    references = set()
    for i in range(length):
        references.add(id(node))
        node = node.next_node
    return references

def test_base(arg_for_seed, length, L = None):
    if L == None:
        length = (abs(int(length)) + 2) * 2
        seed(arg_for_seed)
        L = [randrange(100) for _ in range(length)]
    else:
        length = len(L)

    LL = ExtendedLinkedList(L)
    LL.print()
    references = collect_references(LL, length)
    LL.rearrange()

    if collect_references(LL, length) != references:
        print('You cheated!')
        sys.exit()
    else:
        LL.print()

def test_1_pdf(arg_for_seed, length):
    '''
    >>> test_1_pdf(0, 0)
    49, 97, 53, 5
    5, 53, 97, 49
    >>> test_1_pdf(0, 1)
    49, 97, 53, 5, 33, 65
    5, 53, 65, 33, 97, 49
    >>> test_1_pdf(0, 2)
    49, 97, 53, 5, 33, 65, 62, 51
    5, 53, 65, 33, 51, 62, 97, 49
    >>> test_1_pdf(0, 3)
    49, 97, 53, 5, 33, 65, 62, 51, 38, 61
    5, 53, 65, 33, 51, 62, 61, 38, 97, 49
    >>> test_1_pdf(10, 3)
    73, 4, 54, 61, 73, 1, 26, 59, 62, 35
    1, 73, 59, 26, 35, 62, 4, 73, 61, 54
    >>> test_1_pdf(0, 4)
    49, 97, 53, 5, 33, 65, 62, 51, 38, 61, 45, 74
    5, 53, 65, 33, 51, 62, 61, 38, 74, 45, 97, 49
    >>> test_1_pdf(10, 4)
    73, 4, 54, 61, 73, 1, 26, 59, 62, 35, 83, 20
    1, 73, 59, 26, 35, 62, 20, 83, 4, 73, 61, 54
    >>> test_1_pdf(20, 6)
    92, 87, 98, 19, 33, 86, 81, 12, 41, 73, 21, 3, 52, 52, 9, 13
    3, 21, 52, 52, 13, 9, 87, 92, 19, 98, 86, 33, 12, 81, 73, 41
    >>> test_1_pdf(30, 6)
    69, 37, 78, 3, 79, 83, 26, 32, 6, 50, 48, 82, 17, 10, 59, 0
    0, 59, 37, 69, 3, 78, 83, 79, 32, 26, 50, 6, 82, 48, 10, 17
    '''
    test_base(arg_for_seed, length)
    pass

def test_2_regular_nums(L):
    '''
    >>> test_2_regular_nums([10, 9, 2, 1, 4, 3, 6, 5, 8, 7])
    10, 9, 2, 1, 4, 3, 6, 5, 8, 7
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    >>> test_2_regular_nums([6, 5, 8, 7, 10, 9, 2, 1, 4, 3])
    6, 5, 8, 7, 10, 9, 2, 1, 4, 3
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    '''
    test_base(0, 0, L)
    pass

def test_3_edge_cases(L):
    '''
    >>> test_3_edge_cases([0, 0, 0, 0])
    0, 0, 0, 0
    0, 0, 0, 0
    '''
    test_base(0, 0, L)
    pass

if __name__ == '__main__':
    import doctest
    doctest.testmod()
