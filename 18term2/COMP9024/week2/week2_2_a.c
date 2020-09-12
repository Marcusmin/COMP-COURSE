#include <stdio.h>

float innerProduct(float a[], float b[], int n){
    int i, product;
    
    product = 0;
    
    for(i = 0; i < n; i++){
        product += a[i] * b[i];
    }
    return product;
}
