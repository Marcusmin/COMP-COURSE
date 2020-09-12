class treeNode(object):
    def __init__(self,value = None,left = None,right = None):
        self.value = value
        self.left = left
        self.right = right

    def add_Node_left(self,nextNode):
        if self.left == None:
            self.left = nextNode
        else:
            self.left.left = nextNode
    def add_Node_right(self,nextNode):
        if self.right == None:
            self.right = nextNode
        else:
            self.right.right = nextNode


def print_node(Node):
    if Node == None:
        return
    print(Node.value)
    print_node(Node.left)
    print_node(Node.right)

def build_tree(array):
    root  = treeNode(array[0][0])
    pre_Node = root
    for i in range(1,len(array)):
        for j in range(len(array[i])):
            if j == 0:
                next_node = treeNode(array[i][j])
                pre_Node.add_Node_left(next_node)
            elif j == (len(array[i]) - 1):
                next_node = treeNode(array[i][j])
                pre_Node.add_Node_right(next_node)
            else:
                next_node = treeNode(array[i][j])
                root.add_Node_left(next_node)
                root.add_Node_right(next_node)
        if j % 2 == 1:
            pre_Node = pre_Node.left
    return root
array = [['A'],['B','C'],['D','E','F']]
node = []
root = build_tree(array)
print_node(root)
