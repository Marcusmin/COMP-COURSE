## import modules here 

################# Question 0 #################

def add(a, b): # do not change the heading of the function
    return a + b


################# Question 1 #################

def nsqrt(x): # do not change the heading of the function
    if x == 1:
        return 1
    start = 1
    end = x
    while start <= end:
        mid = (start + end) // 2
        if mid**2 > x:
            end = mid - 1
        elif mid**2 < x:
            start = mid + 1 
        else:
            return mid
    if mid**2 > x:
        return mid - 1
    else:
        return mid
    # **replace** this line with your code


################# Question 2 #################


# x_0: initial guess
# EPSILON: stop when abs(x - x_new) < EPSILON
# MAX_ITER: maximum number of iterations

## NOTE: you must use the default values of the above parameters, do not change them

def find_root(f, fprime, x_0=1.0, EPSILON = 1E-7, MAX_ITER = 1000): # do not change the heading of the function
    i = 0
    x = x_0
    while i < MAX_ITER:
        x_new = x - f(x)/fprime(x)
        print(x, x_new)
        if abs(x - x_new) < EPSILON:
            return x_new
        x = x_new
        i += 1
    return x
    # **replace** this line with your code


################# Question 3 #################

class Tree(object):
    def __init__(self, name='ROOT', children=None):
        self.name = name
        self.children = []
        if children is not None:
            for child in children:
                self.add_child(child)
    def __repr__(self):
        return self.name
    def add_child(self, node):
        assert isinstance(node, Tree)
        self.children.append(node)

from collections import deque
def make_tree(tokens): # do not change the heading of the function
    res = []
    tokens = deque(tokens)
    while tokens:
        token = tokens.popleft()
        if token != ']':
            res.append(token)
        else:
            tok = res.pop()
            L = []
            while tok != '[':
                if isinstance(tok, str):
                    tok = Tree(tok)
                elif isinstance(tok, list):
                    tok = Tree(res.pop(), tok)
                else:
                    print("Panic")
                L.append(tok)
                tok = res.pop()
            res.append(L[::-1])
    res = Tree(res[0], res[1])
    return res
     # **replace** this line with your code    

def max_depth(root): # do not change the heading of the function
    if not root.children:
        return 1
    else:
        depth = 1
        for tree in root.children:
            child_depth = max_depth(tree)
            if child_depth >= depth:
                depth = child_depth
        return 1 + depth
    
    # **replace** this line with your code
