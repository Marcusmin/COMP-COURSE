# Written by *** for COMP9021


from binary_tree_adt import *
from math import log


class PriorityQueue(BinaryTree):
    def __init__(self):
        super().__init__()

    def insert(self, value):
        insert_value = value
        if self.value is None:
            self.value = value
            self.left_node = PriorityQueue()
            self.right_node = PriorityQueue()
        else:
            if self.value > insert_value:
                self.value, insert_value = insert_value, self.value
                self.insert(insert_value)
            else:
                if self.left_node.value is None:
                    return self.left_node.insert(insert_value)
                if self.left_node.value is not None and self.right_node.value is not None:
                    if self.left_node.left_node.value is not None and self.left_node.right_node.value is not None\
                       and self.right_node.right_node.value is None:
                        return self.right_node.insert(insert_value)
                    return self.left_node.insert(insert_value)

                return self.right_node.insert(insert_value)
                
        
            
        # Replace pass above with your code

