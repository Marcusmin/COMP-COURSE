#include <assert.h>
#include <stdlib.h>
#include "Stack.h"
Stack InitStack(void){
   Stack stackp = (Stack)malloc(sizeof(StackRep));
   stackp->top = -1;
   return stackp;
}

int StackIsEmpty(Stack stackp){
   if(stackp->top < 0){
      return 1;
   }
   else{
      return 0;
   }
}

int StackPop(Stack stackp){
   int popItem;
   assert(stackp->top >= 0);
   popItem = stackp->item[stackp->top--];
   return popItem;
}

void StackPush(Stack stackp, int newItem){
   assert(stackp->top < MAXLEN - 1);
   stackp->item[++stackp->top] = newItem;
   return;
}
