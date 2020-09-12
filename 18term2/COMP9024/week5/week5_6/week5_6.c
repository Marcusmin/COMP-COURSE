#include <stdio.h>
#include "Stack.h"
int soln(StackRep stack_1, StackRep stack_2, int target);
int main(void){
   Stack stack_1, stack_2;
   
   stack_1 = InitStack();
   stack_2 = InitStack();
   
   StackPush(stack_1, 1);
   StackPush(stack_1, 6);
   StackPush(stack_1, 4);
   StackPush(stack_1, 2);
   StackPush(stack_1, 4);
   
   StackPush(stack_2, 5);
   StackPush(stack_2, 8);
   StackPush(stack_2, 1);
   StackPush(stack_2, 2);
   
   printf("%d\n",soln(*stack_1, *stack_2, 7));
}
int soln(StackRep stack_1, StackRep stack_2, int target){
   int pop_item;
   if(target < 0){
      return 0;
   }
   if(target == 0){
      return 0;
   }
   if(stack_2.item[stack_2.top] <= stack_1.item[stack_2.top]){
      pop_item = StackPop(&stack_2);
   }
   else{
      pop_item = StackPop(&stack_1);
   }
   if(pop_item <= target){
      return 1 + soln(stack_1, stack_2, target - pop_item);
   }
   else{
      return 0;
   }
}