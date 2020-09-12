#include <stdio.h>
#define M 3
#define N 4
#define P 4

void matrixProduct(float a[M][N], float b[N][P], float c[M][P]){
    int row, col, k;
    
    for(row = 0; row < M; row ++){
        for(col = 0; col < P; col ++){
            for(k = 0; k < N; k++){
                c[row][col] = a[row][k] * b[k][col];
            }
        }
    }
}
