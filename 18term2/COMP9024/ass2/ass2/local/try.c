#include <stdio.h>
#include <stdlib.h>
int compfunc(const void * a, const void * b){
	return (*(int *)a - *(int *)b);
}
int isAContainB(int A, int B){
	int *digitsOfA = (int *)calloc(10, sizeof(int));
	int *digitsOfB = (int *)calloc(10, sizeof(int));
	int i, j;
	for(i = 0; A / 10; i++, A = A / 10){
		digitsOfA[i] = A % 10;
	}
	digitsOfA[i] = A;
	for(j = 0; B / 10; j++, B = B / 10){
		digitsOfB[j] = B % 10;
	}
	digitsOfB[j] = B;
	qsort(digitsOfA, i + 1, sizeof(int), compfunc);
	qsort(digitsOfB, j + 1, sizeof(int), compfunc);
	// for(int k = 0; k <= i; k++){
	// 	printf("%d\t",digitsOfA[k]);
	// }
	// printf("\n");
	for(int a = 0,b = 0; b <= j;b++){
		while(a <= i && digitsOfA[a] < digitsOfB[b]){
			a ++;
		}
		if(a > i || digitsOfA[a] > digitsOfB[b]){
			return 0;
		}
	}
	return 1;
}

int try2Darray(int i){
	int **a[i][i];
	for(int j = 0; j < i; j++){
		for(int k = 0; k < i; k++){
			a[j][k] = 0;
		}
	}
	return 1;
}
int main(int argc, char const *argv[]){
	int a = 192;
	int b = 2;
	if(isAContainB(a, b)){
		printf("%d contains %d\n",a, b);
	}else{
		printf("%d doesn't contain %d\n",a, b);
	}
}