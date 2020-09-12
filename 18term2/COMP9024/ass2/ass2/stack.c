// Stack ADT implementation ... COMP9024 18s2

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include "stack.h"

typedef struct node {
    int data;
    struct node *next;
} NodeT;

typedef struct StackRep {
    int    height;
    NodeT *top;
} StackRep;

// set up empty stack
stack newStack() {
    stack S = malloc(sizeof(StackRep));
    S->height = 0;
    S->top = NULL;
    return S;
}
//height of stack
int heightStack(stack s){
    return s->height;
}
// remove unwanted stack
void dropStack(stack S) {
    NodeT *curr = S->top;
    while (curr != NULL) {
        NodeT *temp = curr->next;
        free(curr);
        curr = temp;
    }
    free(S);
}

// check whether stack is empty
int StackIsEmpty(stack S) {
    return (S->height == 0);
}

// insert an int on top of stack
void StackPush(stack S, int v) {
    NodeT *new = malloc(sizeof(NodeT));
    assert(new != NULL);
    new->data = v;
    new->next = S->top;
    S->top = new;
    S->height++;
}

// remove int from top of stack
int StackPop(stack S) {
    assert(S->height > 0);
    NodeT *head = S->top;
    S->top = S->top->next;
    S->height--;
    int d = head->data;
    free(head);
    return d;
}
// a stack can store another stack
typedef struct DFSNode {
    stack data;
    struct DFSNode *next;
} DFSNodeT;

typedef struct DFSStackRep {
    int    height;
    DFSNodeT *top;
} DFSStackRep;

DFSstack newDFSStack() {
    DFSstack S = malloc(sizeof(DFSStackRep));
    S->height = 0;
    S->top = NULL;
    return S;
}
//height of stack
int heightDFSStack(DFSstack s){
    return s->height;
}
// remove unwanted stack
void dropDFSStack(DFSstack S) {
    DFSNodeT *curr = S->top;
    while (curr != NULL) {
        DFSNodeT *temp = curr->next;
        dropStack(curr->data);
        free(curr);
        curr = temp;
    }
    free(S);
}

// check whether stack is empty
int DFSStackIsEmpty(DFSstack S) {
    return (S->height == 0);
}

// insert an stack on top of stack
void DFSStackPush(DFSstack S, stack v) {
    DFSNodeT *new = malloc(sizeof(DFSNodeT));
    assert(new != NULL);
    new->data = v;
    new->next = S->top;
    S->top = new;
    S->height++;
}

// remove stack from top of DFSstack
stack DFSStackPop(DFSstack S) {
    assert(S->height > 0);
    DFSNodeT *head = S->top;
    S->top = S->top->next;
    S->height--;
    stack d = head->data;
    free(head);
    return d;
}

stack CopyStack(stack s){
    stack temp = newStack();
    stack copyOfStackOne = newStack();
    stack tempTwo = newStack();
    while(!StackIsEmpty(s)){
        int node = StackPop(s);
        StackPush(temp, node);
        StackPush(tempTwo, node);
    }
    while(!StackIsEmpty(temp)){
        int node = StackPop(temp);
        StackPush(copyOfStackOne, node);
        //StackPush(copyOfStackTwo, node);
    }//one stack become two stack
    //recover original stack
    while(!StackIsEmpty(tempTwo)){
        int node = StackPop(tempTwo);
        StackPush(s, node);
    }
    dropStack(temp);
    dropStack(tempTwo);
    return copyOfStackOne;
}

void showStack(stack s){
    stack temp = CopyStack(s);
    printf("{ ");
    while(!StackIsEmpty(temp)){
        printf("%d ", StackPop(temp));
    }
    printf("}\n");
}

int isInStack(stack s, int i){
    stack temp = CopyStack(s);
    while(! StackIsEmpty(temp)){
        if(StackPop(temp) == i){
            dropStack(temp);
            return 1;
        }
    }
    dropStack(temp);
    return 0;
}
int min(int i, int j){
    if(i > j){
        return j;
    }else{
        return i;
    }
}
int stackcompfunc(const void * a, const void * b){
	stack tempA = CopyStack(*(stack *)a);
    stack tempB = CopyStack(*(stack *)b);
    int A[tempA->height];
    int B[tempB->height];
    int i = 0, j = 0;
    while(!StackIsEmpty(tempA)){
        A[i] = StackPop(tempA);
        i++;
    }
    while(!StackIsEmpty(tempB)){
        B[j] = StackPop(tempB);
        j++;
    }
    dropStack(tempA);
    dropStack(tempB);
    for(i = min(i - 1, j - 1);i > 0 ;i--){
        if(A[i] - B[i] == 0){
            continue;
        }else{
            break;
        }
    }
    // printf("%d\n",A[i] - B[i]);
    return -(A[i] - B[i]);
}

void DFSstackSort(DFSstack s){
    stack member[s->height];
    int i = 0;
    while(!DFSStackIsEmpty(s)){
        member[i] = DFSStackPop(s);
        i++;
    }
    qsort(member, i, sizeof(stack), stackcompfunc);
    for(int j = 0; j < i; j++){
        DFSStackPush(s, member[j]);
    }
}