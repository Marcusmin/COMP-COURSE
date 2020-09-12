#include <stdio.h>
#include <stdlib.h>

typedef struct nodeType{
    int data;
    struct nodeType *next;
}NodeT;
NodeT *makeNode(int v);
void showLL(NodeT *list);
void freeLL(NodeT *list);
NodeT *joinLL(NodeT *head1, NodeT *head2);