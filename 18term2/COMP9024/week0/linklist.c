#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "linklist.h"

NodeT *makeNode(int v) {
    NodeT *new = malloc(sizeof(NodeT));
    assert(new != NULL);
    new->data = v;       // initialise data
    new->next = NULL;    // initialise link to next node
    return new;          // return pointer to new node
}

void showLL(NodeT *list) {
    NodeT *p;
    if(list == NULL){
        return;
    }
    for (p = list; p->next != NULL; p = p->next)
        printf("%d->", p->data);
    printf("%d\n",p->data);
}

void freeLL(NodeT *list) {
    NodeT *p;
    
    p = list;
    while (p != NULL) {
        NodeT *temp = p->next;
        free(p);
        p = temp;
    }
}

NodeT *joinLL(NodeT *head1, NodeT *head2){
    NodeT *p;
    
    if(head1 == NULL){
        head1 = head2;
        return head1;
    }
    p = head1;
    while(p->next != NULL){
        p = p->next;
    }
    p->next = head2;
    return head1;
}