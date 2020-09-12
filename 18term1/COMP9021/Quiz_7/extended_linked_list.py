# Written by **** for COMP9021

from linked_list_adt import *

class ExtendedLinkedList(LinkedList):
    def __init__(self, L = None):
        super().__init__(L)

    def rearrange(self):
        #find the smallest value in linked list
        node = self.head
        smallest_value = self.head.value
        while node:
            if smallest_value > node.value:
                smallest_value = node.value
            if not node.next_node:
                break
            node = node.next_node
        ##print('smallest',smallest_value)
        #make the linked list become a circle
        node.next_node = self.head
        #find the node whose value is the smallest
        node = self.head
        while node.next_node.next_node and node.next_node.next_node.value != smallest_value :
            ##print(node.value)
            node = node.next_node
        ##print(node.value)
        smallest_node = node.next_node.next_node
        ##print(smallest_node.value)
##        exchange_node_1 = node.next_node
##        exchange_node_2 = node.next_node.next_node
##        exchange_node_1.next_node = exchange_node_2.next_node
##        exchange_node_2.next_node = exchange_node_1
##        node.next_node = exchange_node_2
        ##print(node.value,node.next_node.value,node.next_node.next_node.value)
##        node = node.next_node.next_node
        while node.next_node != smallest_node:
            exchange_node_1 = node.next_node
            exchange_node_2 = node.next_node.next_node
            exchange_node_1.next_node = exchange_node_2.next_node
            exchange_node_2.next_node = exchange_node_1
            node.next_node = exchange_node_2
            ##print(node.value,node.next_node.value,node.next_node.next_node.value)
            node = node.next_node.next_node
        self.head = node.next_node
        node.next_node = None
            
        
        
        
        
        # Replace pass above with your code
    
    
    
