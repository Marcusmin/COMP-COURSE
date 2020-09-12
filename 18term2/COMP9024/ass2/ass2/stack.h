// Stack ADT header file ... COMP9024 18s2

typedef struct StackRep *stack;
typedef struct DFSStackRep *DFSstack;

stack newStack();             // set up empty stack
void  dropStack(stack);       // remove unwanted stack
int   StackIsEmpty(stack);    // check whether stack is empty
void  StackPush(stack, int);  // insert an int on top of stack
int   StackPop(stack);        // remove int from top of stack
int heightStack(stack s);


stack DFSStackPop(DFSstack S);
void DFSStackPush(DFSstack S, stack v);
int DFSStackIsEmpty(DFSstack S);
void dropDFSStack(DFSstack S);
int heightDFSStack(DFSstack s);
DFSstack newDFSStack();
void showStack(stack s);
stack CopyStack(stack s);
void DFSstackSort(DFSstack s);