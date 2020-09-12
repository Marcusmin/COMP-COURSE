#include <stdio.h>
#include "Stack.h"

int stoi(char* string);

int main(int argc, char* argv[]){
    int i, pop_item;
    
    if(argc < 2){
        printf("No argument");
        return 1;
    }
    else{
        StackInit();
        for(i = 1; i < argc; i++){
            StackPush(stoi(argv[i]));
        }
        while(!StackIsEmpty()){
            pop_item = StackPop();
            printf("%d\n",pop_item);
        }
    }
    
    return 0;
}

int stoi(char* string){
    int i, num;
    
    num = 0;
    
    for(i = 0; string[i] != '\0'; i++){
        num += string[i] - '0';
        num *= 10;
    }
    return num / 10;
}