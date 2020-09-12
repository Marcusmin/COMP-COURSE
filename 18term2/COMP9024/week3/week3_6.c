#include <stdio.h>

int main(int argc, char* argv[]){
    int i, j;
    
    if(argc < 2){
        printf("Input a command please!\n");
        return 0;
    }
    else{
        for(i = 0; argv[1][i] != '\0'; i++);
        for(;i >= 0;i --){
            for(j = 0; j < i; j ++){
                printf("%c", argv[1][j]);
            }
            printf("\n");
        }
    }
    return 1;
}