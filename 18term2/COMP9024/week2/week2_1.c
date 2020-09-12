#include <stdio.h>

int reverse(int);
int main(){
    int i;
    
    for(i = 10000; i <= 99999; i ++){
        if(4 * i == reverse(i)){
            printf("%d * 4 == %d\n",i,reverse(i));
            return i;
        }
    }
    
    return 0;
}

int reverse(int num){
    int rnum, i;
    
    rnum = 0;
    
    while(num / 10){
        rnum = rnum * 10 + num % 10;
        num /= 10;
    }
    
    return rnum * 10 + num;
}
