#include <stdio.h>

int main(){
    int i;

    scanf("Input a positive number: %d", &i);
    while(i != 1){
        if(i % 2 == 0){
            i = i / 2;
        }else{
            i = 3 * i + 1;
        }
    }
    printf("%d\n",i);
    return 0;
}
