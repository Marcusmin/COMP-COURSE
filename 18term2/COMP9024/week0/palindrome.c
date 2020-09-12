#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]){
   int head = 0;
   int tail = strlen(argv[1]) - 1;
   char *s = argv[1];
   
   while(head <= tail){
      if(s[head] != s[tail]){
         printf("no\n");
         return 0;
      }
      head ++;
      tail --;
   }
   printf("yes\n");
   return 0;
}