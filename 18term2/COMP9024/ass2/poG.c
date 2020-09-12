#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "WGraph.h"
#include "stack.h"

//Find divisors of nums, return a pointer to an array of its divisors
int compfunc(const void * a, const void * b){
	return (*(int *)a - *(int *)b);
}

int *findDivisor(int num){
	int i;//, temp;
	int k = 0;
	int squareRoot = sqrt(num);
	int *nums = (int *)calloc(squareRoot, sizeof(int));
	for(i = 1; i <= squareRoot; i++){
		if(num % i == 0){
			if(num / i == i){
				nums[k++] = i;
			}else{
				nums[k++] = i;
				nums[k++] = num/i;
			}
		}
	}
	int *divisors = (int *)calloc(k + 1, sizeof(int));
	for(i = 0; nums[i] != 0; i++){
		divisors[i] = nums[i];
	}
	qsort(divisors, i, sizeof(int), compfunc);
	/*temp = divisors[0];
	divisor[0] = divisors[i - 1] ;
	divisors[i - 1] = temp;
	*/
	return divisors;
}

int nbOfDivisor(int *divisorPtr){
	int i;
	for(i = 0; divisorPtr[i] != 0; i++);
	return i;
}

Graph createGraph(int num){
	Graph g;
	int *divisors = findDivisor(num);
	int nV = nbOfDivisor(divisors);
	g = newGraph(nV);
	return g;
}

void buildGaph(Graph graphOfPartialOrder, int num){
	int *divisors = findDivisor(num);
	int nbOfD = nbOfDivisor(divisors);
	Edge edge;
	if (nbOfDivisor(divisors) == 1){	//if a number's divisors only have itself
		return;
	}else{	//otherwise, build graph by partial order
		for(int i = 0; i < nbOfD; i++){
			if(divisors[i] != num){	//for every divisor of number except itself
				edge.v = i;
				edge.w = nbOfD - 1;
				edge.weight = 1;	//Create a direction edge, divisor points to dividend
				insertEdge(graphOfPartialOrder, edge);
				//printf("%d -> %d insert\n",divisors[edge.v], divisors[edge.w]);
				buildGaph(graphOfPartialOrder, divisors[i]);
			}
		}
	}
}


int main(int argc, char *argv[]){
	int num = atoi(argv[1]);
	Graph graphOfPartialOrder;
	int *divisors = findDivisor(num);
	stack VertexStack;

	graphOfPartialOrder = createGraph(num);
	buildGaph(graphOfPartialOrder, num);	//Find divisors of input number
	showPatialOrder(graphOfPartialOrder, divisors);
	VertexStack = findLongestPath(graphOfPartialOrder);
	printf("Longest monotonically increasing sequences:\n");
	while(!StackIsEmpty(VertexStack)){
		if(heightStack(VertexStack) == 1){
			break;
		}
		printf("%d ", divisors[StackPop(VertexStack)]);
		printf("< ");
	}
	printf("%d\n", divisors[StackPop(VertexStack)]);
	return 0;
}
