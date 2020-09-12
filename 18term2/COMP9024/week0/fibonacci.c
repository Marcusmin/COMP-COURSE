#include <stdio.h>


int* fibo(int n, int array[]){
    if(n == 0){
        array[0] = 0;
        return array;
    }
    else if(n == 1){
        array[1] = 1;
        return array;
    }
    else{
        array[n] = fibo(n - 2, array)[n - 2] + fibo(n - 1, array)[n - 1];
        return array;
    }
}

int main(){
    int f[11];
    int* fb;
    int i, n;
    
    fb = fibo(10, f);
    for(i = 1; i < 11; i ++){
        printf("Fib[%d] = %d\n", i, fb[i]);
        n = fb[i];
        while(n != 1){
            printf("%d\n",n);
            if(n % 2 == 0){
                n = n / 2;
            }
            else{
                n = 3 * n + 1;
            }
        }
        printf("%d\n", n);
    }
    return 0;
}

