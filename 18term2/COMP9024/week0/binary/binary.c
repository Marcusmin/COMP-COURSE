#include <stdio.h>
#include "Stack.h"

int stoi(char* string){
    int i, num;
    
    num = 0;
    
    for(i = 0; string[i] != '\0'; i++){
        num += string[i] - '0';
        num *= 10;
    }
    return num / 10;
}

int main(int argc, char* argv[]){
    int num, output;
    
    if(argc == 1){
        printf("Please input a positive integer\n");
        return 1;
    }
    
    if(argc > 2){
        printf("Too Many Arguments!");
        return 1;
    }
    else{
        StackInit();
        num = stoi(argv[1]);
        while(num / 2){
            StackPush(num % 2);
            num /= 2;
        }
        StackPush(num);
        output = 0;
        while(!StackIsEmpty()){
            output += StackPop();
            output *= 10;
        }
        printf("%d\n",output/10);
        return output / 10;
    }
}