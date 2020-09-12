#include <stdio.h>

int main(void){
    int num;
    int i;
    
    i = scanf("%d",&num);
    
    if(i){
        printf("Your input number is %d",num);
    }
    else{
        printf("Non-valid\n");
    }
    return 0;
}